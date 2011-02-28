(ns cxr.swing.dialog
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.search.core :as search])
  (:require [cxr.swing.progress :as progress])
  (:require [cxr.globals :as globals]))


(def msg-type { :info    JOptionPane/INFORMATION_MESSAGE
               :error   JOptionPane/ERROR_MESSAGE
               :warn    JOptionPane/WARNING_MESSAGE
               :message JOptionPane/PLAIN_MESSAGE
               })

(defn- dialog
  [parent msg type]
  (JOptionPane/showMessageDialog parent msg type (msg-type (keyword type))))

(defn info
  [parent msg]
  (dialog parent msg "info"))

(defn debug
  [msg]
  (dialog (JPanel.) msg "info"))

(defn ask-open
  [event frame]
  (let [chooser (JFileChooser.)]
    (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
    (let [ ret (.showOpenDialog chooser frame)]
      (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
      (cond
       (= JFileChooser/APPROVE_OPTION ret)
       (do (send progress/determinate (constantly :bounce))
           (send progress/determinate (fn [_] (search/find-files (.getSelectedFile chooser))
                                        (search/get-files)))) ;; backend
       (= JFileChooser/CANCEL_OPTION ret) (debug "canceleshwar")
       :else "error"))))
