(ns cxr.swing.combo
  (:import (java.awt.event ItemEvent))
  (:require [cxr.swing.dialog :as dialog])
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.search.core :as search]))

(def search-types { " keyword search" search/keyword-search  " filename search" search/filename-search " context search" search/context-search }) ;; figure out how to move this into swing.core
(def search-fn (atom (search-types (first (keys search-types)))))

(defn combo-handler
  [event]
  (let [ state-changed (= (.getStateChange event) ItemEvent/SELECTED) ]
    (if state-changed
      (dosync (reset! search-fn (search-types (str (.getItem event))))))))
