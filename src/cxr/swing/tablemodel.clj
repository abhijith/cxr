(ns cxr.swing.tablemodel
  (:import (javax.swing.table AbstractTableModel))
  (:require [cxr.swing.combo :as combo])
  (:require [cxr.globals :as globals]))

;; (def config {:cols ["Filename"] :data (agent [])}) ;;; id: should store the table config in a declarative model
(def search-column-names ["Results"])
(def index-column-names ["Files" "Indexed"])
(def thesauri-column-names ["Files"])
(def search-table-data (agent []))
(def index-table-data (agent []))
(def thesauri-table-data (agent []))

;; search button handlers
(defn search-show-results
  [a coll]
  (if (and (deref globals/search-running) (not-empty coll))
    (do (send *agent* search-show-results (rest coll))
        (conj @search-table-data [(first coll)]))
    @search-table-data))

(defn search-populate
  [event x]
  (let [word (.getText x)]
    (if-not (empty? word)
      (do
        (dosync (reset! globals/search-running true))
        (send search-table-data search-show-results (lazy-seq ((deref combo/search-fn) word)))))))

(def search-table-model
     (proxy [AbstractTableModel] []
       (getColumnCount []    (count search-column-names))
       (getRowCount    []    (count @search-table-data))
       (getValueAt     [i j] (get-in @search-table-data [i j]))
       (getColumnName  [i]   (search-column-names i))))

(defn init-search-table-data-watch
  []
  (do 
    (add-watch search-table-data :search-table-data
               (fn [k r o n]
                 (.fireTableRowsInserted search-table-model 0 0)))))

;; abort button handler
(defn search-clear-table
  [event]
  (do (dosync (reset! globals/search-running false))
      (send search-table-data (fn [x] [])) ;; use constantly
      (.fireTableRowsDeleted search-table-model 0 0)))

(def index-table-model
     (proxy [AbstractTableModel] []
       (getColumnCount []    (count index-column-names))
       (getRowCount    []    (count @index-table-data))
       (getValueAt     [i j] (get-in @index-table-data [i j]))
       (getColumnName  [i]   (index-column-names i))))

(defn init-index-table-data-watch
  []
  (do 
    (add-watch index-table-data :index-table-data
               (fn [k r o n]
                 (.fireTableRowsInserted index-table-model 0 0)))))

(def thesauri-table-model
     (proxy [AbstractTableModel] []
       (getColumnCount []    (count thesauri-column-names))
       (getRowCount    []    (count @thesauri-table-data))
       (getValueAt     [i j] (get-in @thesauri-table-data [i j]))
       (getColumnName  [i]   (thesauri-column-names i))))

(defn init-thesauri-table-data-watch
  []
  (do 
    (add-watch thesauri-table-data :thesauri-table-data
               (fn [k r o n]
                 (.fireTableRowsInserted thesauri-table-model 0 0)))))
