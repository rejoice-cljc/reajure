(ns reajure.native.components
  (:require
   [reajure.native :refer [defc]]
   [goog.object :as obj]
   ["react-native" :as rn]))

(defn k [o kw]
  (obj/get o (-> kw name str)))

;; ;; todo add default styles
;; ;; opts.style, props.style 
;; ;; todo q: should this emit a macro for calling components (?)

(defc vw
  "View component."
  [^js p]
  [rn/View
   {:style (k p :style)}
   (k p :children)])

(defc txt
  "Text component."
  [^js p]
  [rn/Text
   {:style (k p :style)}
   (k p :children)])

(defc lbl 
  "Label component."
  [^js p]
  [vw
   [txt
    {:style (k p :style)}
    (k p :children)]])

(defc ipt
  "Label component."
  [^js p]
  ;; (println "props.." p)
  [rn/TextInput
  ;;  {:style (k p :style)}
   (k p :children)])
