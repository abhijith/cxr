(ns cxr.swing.tablemodel
  (:gen-class)
  (:import (javax.swing.table AbstractTableModel))
  (:require [cxr.swing.combo :as combo])
  (:require [cxr.globals :as globals]))

;; (def config {:cols ["Filename"] :data (agent [])}) ;;; id: should store the table config in a declarative model
(def search-column-names ["Results"])
(def index-column-names ["Files" "Indexed"])
(def thesauri-column-names ["Files" "Indexed"])
(def search-table-data (agent []))
(def index-table-data (agent []))
(def thesauri-table-data (agent []))

(defn search-populate
  [event x]
  (let [word (.getText x)]
    (if-not (empty? word)
      (do
        (dosync (reset! globals/search-running true))
        (send search-table-data (constantly []))
        (send cxr.swing.progress/search-done (constantly false))
        (send search-table-data (fn [a] (let [ result (into [] (map (fn [[k v]] [(:name k)]) ((deref combo/search-fn) word))) ]
                                         (send cxr.swing.progress/search-done (constantly true))
                                         result))))
      (send search-table-data (constantly [])))))

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
      (send search-table-data (constantly []))
      (send cxr.swing.progress/search-done (constantly true))
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

;; TODO: refactor. Build table abstraction
(defn fill-index-table
  [coll]
  (send index-table-data (fn [a e] e)
        (into []
              (map (fn [rs] [(:name rs) "No"]) coll))))

(defn fill-thesauri-table
  [coll]
  (send thesauri-table-data (fn [a e] e)
        (into []
              (map (fn [rs] [(:name rs) "No"]) coll))))

;; update-(x,y) f & args
(defn update-index-row
  [row]
  (send index-table-data (fn [a] (assoc-in a [row 1] "Yes"))))

(defn update-thesauri-row
  [row]
  (send thesauri-table-data (fn [a] (assoc-in a [row 1] "Yes"))))
