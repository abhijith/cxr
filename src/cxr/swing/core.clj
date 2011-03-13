(ns cxr.swing.core
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog JProgressBar JTabbedPane SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.swing.tablemodel :as table])
  (:require [cxr.swing.menu :as menu])
  (:require [cxr.swing.combo :as combo])
  (:require [cxr.swing.dialog :as dialog])
  (:require [cxr.swing.events :as events])
  (:require [cxr.swing.progress :as progress]))

(defn cxr-ui
  []
  (let [frame  (JFrame. "cxr")
        jtable (JTable. table/search-table-model)
        itable (JTable. table/index-table-model)
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
                 (JProgressBar. 0 10)  {:id :progress-panel :height 20 :width 760 :gapleft 5 :gapright 5 } ) ]
    (doto tpane
      (.addTab "search" panel)
      (.addTab "index" ipanel))
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
      (progress/init-determinate-agent-watch (:progress-panel (components ipanel)) (fn [x] (cxr.search.core/index-file x)
                                                                                     (send cxr.swing.tablemodel/index-table-data conj [x])))
      (progress/init-pb-agent-watch (:progress-panel (components ipanel)))
      (events/add-item-listener combo-box combo/combo-handler)
      (add-action-listener (:index-button (components ipanel)) dialog/ask-open-dir frame)
      (add-action-listener search-button table/search-populate search-box)
      (add-action-listener abort-button table/search-clear-table)
      (events/add-mouse-listener jtable)
      (table/init-index-table-data-watch)
      (table/init-search-table-data-watch))))
