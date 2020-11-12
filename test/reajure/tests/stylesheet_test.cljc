(ns reajure.tests.stylesheet-test
  (:require
   #?(:cljs [cljs.test :refer [deftest testing is]]
      :clj  [clojure.test :refer [deftest testing is]])
   #?(:cljs [goog.object :as obj])
   [reajure.native.stylesheet :as stylesheet]))

(defn obj=
  "Checks whether two objects are equal to each other. 
   In clj, compares the obj s-expr."
  [x1 x2]
  #?(:cljs (obj/equals x1 x2)
     :clj  (= x1 x2)))

(deftest stylesheet-compile 
  (testing "compiles stylesheet shorthands"
    (is (obj= (stylesheet/compile [:flxd-r])
              #?(:cljs #js {:flexDirection "row"}
                 :clj `(cljs.core/js-obj "flexDirection" "row"))))))
