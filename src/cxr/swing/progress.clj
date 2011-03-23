(ns cxr.swing.progress
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.globals :as globals]))


(def pb-agent (agent 0)) ;; update progress of finding files for indexing using the progress as the value of this agent
(def determinate (agent nil))

(defn task
  [x pb f lst start end]
  (if (and (deref globals/index-running) (not (= start end)))
    (do (send *agent* task pb f (rest lst) (inc start) end)
        ;; essence can be pulled out of this function; apply f args or a macro (would work out better?)
        (let [fname (:name (first lst))]
          (if globals/index-running
            (do
              (doto pb
                (.setString (str "indexing" " " fname))
                (.setStringPainted true))
              (f fname x)
              (.setString pb (str ""))))
          (inc x)))
    (do (reset! globals/index-running false) end)))

;; change the mode of the progress bar from indeterminate to determinate and set the start and end values after find-files has finished
(defn init-determinate-agent-watch
  [pb f1 f2] 
  (add-watch determinate
             :determinate
             (fn [k r o n]
               (if (= n :bounce)
                 (do
                   (doto pb
                     (.setIndeterminate true)
                     (.setString "Finding files ...")
                     (.setStringPainted true)))
                   (let [ lst (deref *agent*) end (count lst) ]
                     (doto pb
                       (.setIndeterminate false)
                       (.setMaximum end))
                     (f1 (into [] lst))
                     (send pb-agent task pb f2 lst 0 end))))))

(defn init-pb-agent-watch
  [pb] ;; set the pb-agent's value as the progress value when the value of pb-agent changes
  (add-watch pb-agent :pb-agent
             (fn [k r o n]
               (.setValue pb n) pb)))

;; search and thes progress bar
;; this progress bar agent has two states: true or false 
(def search-done (agent false))

(defn init-search-done-watch
  [pb]
  (add-watch search-done :search-done
             (fn [k r o n]
               (.setIndeterminate pb (not n)))))


(def thes-done (agent false))

(defn init-thes-done-watch
  [pb]
  (add-watch thes-done :thes-done
             (fn [k r o n]
               (.setIndeterminate pb (not n)))))
