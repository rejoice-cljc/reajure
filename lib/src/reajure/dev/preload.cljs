(ns reajure.dev.preload
  (:require [rewrap.dev.refresh :as refresh]))

(refresh/setup!)

(defn ^:dev/after-load refresh []
  (refresh/perform!))
