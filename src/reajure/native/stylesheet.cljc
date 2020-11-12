(ns reajure.native.stylesheet
  (:refer-clojure :exclude [compile])
  (:require [clojure.string :as str]))

(defn- shorthand 
  "Create style shorthand from prefix and suffix pairs."
  [key-prefix val-suffix]
  (keyword (str key-prefix "-" val-suffix)))

(defn- unit-value
  "Calcute style unit value based on factor and rem."
  [fac rem]
  (if (string? fac) fac (int (* fac rem))))
  
(def sstyles
  "Create shorthand styles map."
  (memoize
   (fn [{:keys [rem]
         :as _opts}]
     (letfn [;; -- view styles 
             (flex-styles
               []
               {:flxg-1  {:flexGrow 1}
                :flxs-0  {:flexShrink 0}
                :flxd-r  {:flexDirection "row"}
                :flxd-rr {:flexDirection "row-reverse"}
                :flxd-cr {:flexDirection "column-reverse"}
                :flxw-w  {:flexWrap "wrap"}
                :ai-fs   {:alignItems "flex-start"}
                :ai-c    {:alignItems "center"}
                :ai-fe   {:alignItems "flex-end"}
                :jc-c    {:justifyContent "center"}
                :jc-fs   {:justifyContent "flex-start"}
                :jc-fe   {:justifyContent "flex-end"}
                :jc-sb   {:justifyContent "space-between"}
                :jc-sa   {:justifyContent "space-around"}
                :as-fs   {:alignSelf "flex-start"}
                :as-fe   {:alignSelf "flex-end"}
                :as-c    {:alignSelf "center"}
                :as-s    {:alignSelf "stretch"}
                :of-h    {:overflow "hidden"}})
             (spacing-styles
               []
               (into {}
                     (for [[idx fac] (map-indexed vector [0 0.25 0.5 1 2 4 8])
                           [pre k]   [["m"  :margin]
                                      ["ml" :marginLeft]
                                      ["mr" :marginRight]
                                      ["mt" :marginTop]
                                      ["mb" :marginBottom]
                                      ["mh" :marginHorizontal]
                                      ["mv" :marginVertical]
                                      ["p"  :padding]
                                      ["pl" :paddingLeft]
                                      ["pr" :paddingRight]
                                      ["pt" :paddingTop]
                                      ["pb" :paddingBottom]
                                      ["ph" :paddingHorizontal]
                                      ["pv" :paddingVertical]]]
                       [(shorthand pre idx) {k (unit-value fac rem)}])))

             (dimension-styles
               []
               (into {}
                     (for [[idx fac] (map-indexed vector [0 1 2 4 8 16 32])
                           [pre k]   [["h"   :height]
                                      ["w"   :width]
                                      ["mxh" :maxHeight]
                                      ["mxw" :maxWidth]
                                      ["mnh" :minHeight]
                                      ["mnw" :minWidth]]]
                       [(shorthand pre idx) {k (unit-value fac rem)}])))
             (position-styles
               []
               (into {:pos-abs {:position "absolute"}
                      :pos-abs0 {:position "absolute" :top 0 :left 0 :right 0 :bottom 0}}
                     (for [[idx fac] (map-indexed vector [0 1 2])
                           [pre k] [["tp" :top]
                                    ["rt" :right]
                                    ["lt" :left]
                                    ["bt" :bottom]]]
                       [(shorthand pre idx) {k (unit-value fac rem)}])))
             (border-styles
               []
               (into {}
                     (for [[idx fac] (map-indexed vector [0 0.125 0.25 0.5 1 2])
                           [pre k]   [["bw"  :borderWidth]
                                      ["bwl" :borderLeftWidth]
                                      ["bwr" :borderRightWidth]
                                      ["bwt" :borderTopWidth]
                                      ["bwb" :borderBottomWidth]]]
                       [(shorthand pre idx) {k (unit-value fac rem)}])))
             (opacity-styles
               []
               (into {}
                     (for [[suffix unit] (into
                                       ;; pair each unit with its percentage string
                                          (mapv #(vector (str/replace (str (* % 10)) #"\." "") %)
                                                [0 0.025 0.05 0.1 0.2 0.3 0.4 0.5 0.6 0.6 0.7 0.8 0.9])
                                          [["100" 1]])]
                       [(shorthand "o" suffix) {:opacity unit}])))
          ;; --- text styles
             (font-styles
               []
               (into {;; note: "fs" is for fontSize, so we use "fm" (fontMark) for :fontStyle and :fontWeight keys.
                      :fm-i  {:fontStyle "italic"}
                      :fm-n  {:fontWeight "bold"}
                      :ta-lt {:textAlign "left"}
                      :ta-c  {:textAlign "center"}
                      :ta-rt {:textAlign "right"}
                      :ta-j  {:textAlign "justify"}
                      :td-u  {:textDecorationLine "underline"
                              :textDecorationStyle "solid"}
                      :td-th {:textDecorationLine "line-through"
                              :textDecorationStyle "solid"}}
                     (for [[idx fac] (map-indexed vector [0 0.875 1 1.25 1.5 2.125 2.75 5 6])]
                       [(shorthand "fs" idx) {:fontSize (unit-value fac rem)}])))]
       (merge (flex-styles)
              (spacing-styles)
              (dimension-styles)
              (position-styles)
              (border-styles)
              (opacity-styles)
              (font-styles))))))

(comment
  (sstyles {:rem 16}))

(defn- parse-style-value
  "Expands any shorthand style value map into key-value seq."
  [x sstyles]
  (if (keyword? x)
    (if-let [style-map (get sstyles x)]
      (mapv
       (fn [[kw v]] [(-> kw name str) v])
       (seq style-map))
      (throw (ex-info "Style shorthand not found." {:key x})))
    x))

(defn compile 
  ([style] (compile style {:rem 16}))
  ([style opts]
   (let [sstyles (sstyles opts)
         styles-keyvals (flatten (map #(parse-style-value % sstyles) style))]
     #?(:cljs (apply cljs.core/js-obj styles-keyvals)
        :clj  `(cljs.core/js-obj ~@styles-keyvals)))))
