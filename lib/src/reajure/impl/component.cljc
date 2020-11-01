(ns reajure.impl.react
  (:require
   #?@(:cljs [["react" :as react]]
       :clj [[rewrap.hiccup :as hiccup]
             [rewrap.component :as comp]]))
  #?(:cljs (:require-macros [reajure.impl.react])))

#?(:cljs (def createElement react/createElement))

#?(:clj
   (do
     (defn hiccup "Compile component hiccup fn."
       [expr]
       (letfn [(emit-element [type props & children] `(createElement ~type ~props ~@children))]
         (let [parsers {keyword? {:tag #(-> % name str)}
                        any?     {:props comp/->props}}]
           (hiccup/compile expr {:emitter emit-element
                                 :parsers parsers}))))

     (defmacro defc "Define fn component."
       [& forms]
       (letfn [(parse-body [exprs] `[~@(butlast exprs) ~(hiccup (last exprs))])]
         (let [{:keys [name docstr component]} (comp/compile forms {:body parse-body})]
           `(def ~@(if docstr [name docstr] [name])
              ~component))))))
