(ns cxr.swing.core
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.swing.tablemodel :as table])
  (:require [cxr.swing.menu :as menu])
  (:require [cxr.swing.dialog :as dialog])
  (:require [cxr.swing.combo :as combo])
  (:require [cxr.swing.events :as events]))

(defn cxr-ui
  []
  (let [ frame  (JFrame. "cxr")
         jtable (JTable. table/table-model)
	 search-box (JTextField. "")
	 search-button (JButton. "search")
	 abort-button  (JButton. "abort")
	 combo-box (JComboBox. (into-array [1 2 3]))
	 panel  (miglayout
		  (JPanel.)
		  (miglayout (JPanel.)) {:id :search-panel } :wrap
		  (JScrollPane. jtable) {:id :result-panel :height 400 :width 760 :gapleft 5 }
		  (miglayout (JPanel.)) {:id :status-panel } ) ]
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
    (doto frame
      (.setJMenuBar (menu/menubar frame))
      (.add panel)
      (.setLocation 300 180)
      (.setResizable false)
      (.pack)
      (.setVisible true))
    (do (events/add-item-listener combo-box combo/combo-handler)
        (add-action-listener search-button table/populate search-box)
        (add-action-listener abort-button table/clear-table)
        (events/add-mouse-listener jtable))))

        
