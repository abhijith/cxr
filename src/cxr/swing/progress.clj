(ns cxr.swing.progress
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.search.core :as search])
  (:require [cxr.globals :as globals]))


(def pb-agent (agent 0)) ;; update progress of finding files for indexing using the progress as the value of this agent
(def determinate (agent nil))

(defn task
  [x lst start end]
  (if (and (deref globals/running) (not (= start end)))
    (do (send *agent* task (rest lst) (inc start) end)
        ;; essence can be pulled out of this function; apply f args or a macro (would work out better?)
        (let [fname (first lst)]
          (if globals/running
            (do
              (search/index-file fname)))
          (inc x)))
    (do (reset! globals/running false) end)))

;; change the mode of the progress bar from indeterminate to determinate and set the start and end values after find-files has finished
(defn init-determinate-agent-watch
  [pb] 
  (add-watch determinate
             :determinate
             (fn [k r o n]
               (if (= n :bounce) (.setIndeterminate pb true)
                   (let [ lst (deref *agent*) end (count lst) ]
                     (.setIndeterminate pb false)
                     (.setMaximum pb end) 
                     (send pb-agent task lst 0 end))))))

(defn init-pb-agent-watch
  [pb] ;; set the pb-agent's value as the progress value when the value of pb-agent changes
  (add-watch pb-agent :pb-agent
             (fn [k r o n]
               (.setValue pb n) pb)))
