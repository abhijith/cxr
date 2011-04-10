(ns cxr.swing.core
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog JProgressBar JTabbedPane SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.swing.tablemodel :as table] :reload)
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
        abort-button  (JButton. "abort")
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
      (.add search-button "")
      (.add abort-button ""))
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
      (progress/init-determinate-agent-watch (:progress-panel (components ipanel))
                                             (fn [x] (send cxr.swing.tablemodel/index-table-data (fn [a e] e) (into []
                                                                                                                  (map (fn [e] [(:name e) (:indexed e)]) x))))
                                             (fn [x i]
                                               (cxr.search.core/index-file x)
                                               (send cxr.swing.tablemodel/index-table-data (fn [a]
                                                                                             (assoc-in a [i 1] true)))))
      (add-action-listener (:button (components settings)) dialog/ask-open-file frame
                           (fn [x]
                             (send cxr.swing.progress/thes-done (constantly false))
                             (send cxr.swing.tablemodel/thesauri-table-data (fn [a] 
                                                                              (let [ result (cxr.search.core/add-thes x) ]
                                                                                (send cxr.swing.progress/thes-done (constantly true))
                                                                                ;; code to add the thesauri to the list grid
                                                                                [[]])))))
      (progress/init-pb-agent-watch (:progress-panel (components ipanel)))
      (progress/init-search-done-watch (:progress-panel (components panel)))
      (progress/init-thes-done-watch (:progress-panel (components settings)))
      (events/add-item-listener combo-box combo/combo-handler)
      (add-action-listener (:index-button (components ipanel)) dialog/ask-open-dir frame
                           (fn [agent x] (cxr.search.core/find-files x)
                             (cxr.search.core/get-files)))
      (add-action-listener search-button table/search-populate search-box)
      (add-action-listener abort-button table/search-clear-table)
      (events/add-mouse-listener jtable)
      (table/init-index-table-data-watch)
      (table/init-thesauri-table-data-watch)
      (table/init-search-table-data-watch))))
