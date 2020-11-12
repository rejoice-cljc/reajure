(ns reajure.core
  (:require
   #?(:cljs [reajure.native]
      :clj  [reajure.native :as rn])
   #?(:cljs [reajure.native.components]))
  #?(:cljs (:require-macros [reajure.core])))

#?(:clj
   (defmacro defc [& decls]
     (rn/fc* decls
             {:parsers {keyword? {:tag #(symbol "reajure.native.components" (name %))}}})))
