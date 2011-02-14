(ns cxr.swing.tablemodel
  (:import (javax.swing.table AbstractTableModel))
  (:require [cxr.swing.combo :as combo])
  (:require [cxr.globals :as globals]))

;; (def config {:cols ["Filename"] :data (agent [])}) ;;; id: should store the table config in a declarative model
(def column-names ["Filename"])
(def table-data (agent []))

;; search button handlers
(defn show-results
  [a coll]
  (if (and (deref globals/running) (not-empty coll))
    (do (send *agent* show-results (rest coll))
        (conj @table-data [(first coll)]))
    @table-data))

(defn populate
  [event x]
  (let [word (.getText x)]
    (if-not (empty? word)
      (do
        (dosync (reset! globals/running true))
        (send table-data show-results (lazy-seq ((deref combo/search-fn) word)))))))

(def table-model
     (proxy [AbstractTableModel] []
       (getColumnCount []    (count column-names))
       (getRowCount    []    (count @table-data))
       (getValueAt     [i j] (get-in @table-data [i j]))
       (getColumnName  [i]   (column-names i))))

(defn init-table-data-watch
  []
  (do 
    (add-watch table-data :table-data
               (fn [k r o n]
                 (.fireTableRowsInserted table-model 0 0)))))

;; abort button handler
(defn clear-table
  [event]
  (do (dosync (reset! globals/running false))
      (send table-data (fn [x] []))
      (.fireTableRowsDeleted table-model 0 0)))
