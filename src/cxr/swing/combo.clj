(ns cxr.swing.combo
  (:import (java.awt.event ItemEvent))
  (:require [cxr.swing.dialog :as dialog])
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)])))

;; globals for search button 
(def search-types { " keyword search" (fn [x] (find x true))  " filename search" find " context search" find })
(def search-fn (search-types " context search"))

(defn combo-handler
  [event]
  (let [ state-changed (= (.getStateChange event) ItemEvent/SELECTED) ]
    (if state-changed
      (do (dialog/debug (str (.getItem event)))
          (def search-fn (search-types (str (.getItem event))))))))
