(ns reajure.native
  (:require [reajure.impl.component :refer [defc]]
            ["react-native" :as rn]))

(defn children [props] 
  (.-children props))

(defc vw [props]
  [rn/View 
   (children props)])

(defc txt [props]
  [rn/Text
   (children props)])
