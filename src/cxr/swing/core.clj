(ns cxr.swing.core
  (:gen-class)
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog JProgressBar JTabbedPane SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.swing.tablemodel :as table] :reload)
  (:require [cxr.db.tables :as db] :reload)
  (:require [cxr.swing.menu :as menu] :reload)
  (:require [cxr.swing.combo :as combo] :reload)
  (:require [cxr.swing.dialog :as dialog] :reload)
  (:require [cxr.swing.events :as events] :reload)
  (:require [cxr.swing.progress :as progress] :reload))

(defn cxr-ui
  []
  (let [frame  (JFrame. "cxr")
        jtable (JTable. table/search-table-model)
        itable (JTable. table/index-table-model)
        sw-table (JTable. table/thesauri-table-model)
        search-box (JTextField. "")
        search-button (JButton. "search")
        combo-box (JComboBox. (into-array (keys combo/search-types)))
        tpane (JTabbedPane.)
        panel  (miglayout
                (JPanel.)
                (miglayout (JPanel.)) {:id :search-panel } :wrap
                (JScrollPane. jtable) {:id :result-panel :height 400 :width 760 :gapleft 5 :gapright 5 } :wrap
                (JProgressBar. 0 0) {:id :progress-panel :height 20 :width 760 :gapleft 5 :gapright 5 } )
        ipanel  (miglayout
                 (JPanel.)
                 (JButton. "index") {:id :index-button :gapleft 5 :gapright 5 } :wrap
                 (JScrollPane. itable) {:id :result-panel :height 400 :width 760 :gapleft 5 :gapright 5 } :wrap
                 (JProgressBar. 0 10)  {:id :progress-panel :height 20 :width 760 :gapleft 5 :gapright 5 } )
        settings  (miglayout
                   (JPanel.)
                   (JButton. "add") {:id :button :gapleft 5 :gapright 5 } :wrap
                   (JScrollPane. sw-table) {:id :sw-panel :height 400 :width 760 :gapleft 5 :gapright 5 } :wrap
                   (JProgressBar. 0 10)  {:id :progress-panel :height 20 :width 760 :gapleft 5 :gapright 5 } ) ]
    (doto tpane
      (.addTab "search" panel)
      (.addTab "index" ipanel)
      (.addTab "thesauri" settings))
    (doto (:search-panel (components panel))
      (.add combo-box "w 100")
      (.add search-box "h 22, w 300, gapleft 20")
      (.add search-button ""))
    (doto jtable
      (.setShowGrid false)
      (.setShowHorizontalLines true)
      (.setShowVerticalLines true)
      (.setRowHeight 20)
      (.setGridColor Color/black))
    (doto itable
      (.setShowGrid false)
      (.setShowHorizontalLines true)
      (.setShowVerticalLines true)
      (.setRowHeight 20)
      (.setGridColor Color/black))
    (doto frame
      (.add tpane)
      (.setLocation 300 180)
      (.setResizable false)
      (.pack)
      (.setVisible true))
    (do
      (add-action-listener search-box table/search-populate search-box)
      (progress/init-search-done-watch (:progress-panel (components panel)))
      (events/add-item-listener combo-box combo/combo-handler)

      (add-action-listener (:index-button (components ipanel))
                           dialog/ask-open-dir 
                           frame 
                           (fn [chooser]
                             (progress/indeterminate-progress-bar
                              (:progress-panel (components ipanel))
                              [(fn []
                                 (cxr.search.core/find-files (.getAbsolutePath (.getSelectedFile chooser)))
                                 (let [coll (cxr.search.core/get-files)]
                                   (table/fill-index-table coll)
                                   coll))]
                              [(fn [agent-val element button]
                                 (if (= :nil element)
                                   (.setEnabled button true)
                                   (do
                                     (.setEnabled button false)
                                     (cxr.search.core/index-file (:name element))
                                     (table/update-index-row (:current agent-val))))) (:index-button (components ipanel))])))

      (add-action-listener (:button (components settings))
                           dialog/ask-open-dir 
                           frame
                           (fn [chooser]
                             (progress/indeterminate-progress-bar
                              (:progress-panel (components settings))
                              [(fn []
                                 (cxr.search.core/find-thesauri (.getAbsolutePath (.getSelectedFile chooser)))
                                 (let [coll (cxr.search.core/get-thesauri)]
                                   (table/fill-thesauri-table coll)
                                   coll))]
                              [(fn [agent-val element button]
                                 (if (= :nil element)
                                   (.setEnabled button true)
                                   (do
                                     (.setEnabled button false)
                                     (cxr.search.core/add-thes (:name element))
                                     (table/update-thesauri-row (:current agent-val))))) (:button (components settings ))])))
      
      (add-action-listener search-button table/search-populate search-box)
      (events/add-mouse-listener jtable)
      (table/init-index-table-data-watch)
      (table/init-thesauri-table-data-watch)
      (table/init-search-table-data-watch))))

(defn -main
  []
  (try
    (db/create-tables)
    (catch Exception e (prn "in catch"))
    (finally (cxr-ui))))
