(ns reajure.core-test
  {:clj-kondo/config '{:linters {:inline-def {:level :off}}}}
  (:require [cljs.test :refer [deftest testing is]]
            [reajure.core :as rj]))

(deftest defc-test
  (testing "renders native components as keywords"
    (rj/defc native [] [:vw [:txt "Foo"]])
    (is (object? (native)))))
