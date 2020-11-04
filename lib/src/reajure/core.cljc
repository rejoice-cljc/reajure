(ns reajure.core
  (:require 
   #?(:cljs [reajure.native]
      :clj  [reajure.native :as rn])
   #?(:cljs [reajure.native.components]))
  #?(:cljs (:require-macros [reajure.core])))

;; todo have rewrap accept parser vector 
;; todo make component opts dynamic

#?(:clj
   (defmacro defc [& decls]
     (rn/fc* decls 
             {:parsers {keyword? {:tag #(symbol "reajure.native.components" (name %))}}})))
