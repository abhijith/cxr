(ns cxr.swing.tablemodel
  (:import (javax.swing.table AbstractTableModel)))

;; cxr global
(def running (atom true))
(def column-names ["Filename"])
(def table-data (agent []))

(defn show-results
  [a coll]
  (if (and @running (not-empty coll))
    (do (send *agent* show-results (rest coll))
        (conj @table-data [(first coll)]))
    @table-data))

(defn populate
  [event x]
  (let [word (.getText x)]
    (if-not (empty? word)
      (do
        (dosync (reset! running true))
        (send table-data show-results [1 2 3])))))

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
  (do (dosync (reset! running false))
      (send table-data (fn [x] []))
      (.fireTableRowsDeleted table-model 0 0)))
