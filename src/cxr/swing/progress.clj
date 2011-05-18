(ns cxr.swing.progress
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.globals :as globals]))

;; change the mode of the progress bar from indeterminate to determinate and set the start and end values after find-files has finished
(def search-done (agent false))

(defn init-search-done-watch
  [pb]
  (add-watch search-done :search-done
             (fn [k r o n]
               (.setIndeterminate pb (not n)))))

(defn add-progress-listener
  [pb coll f & args]
  (let [cnt (count coll)
        task-agent (agent {:start 0 :end cnt :current 0 :element (first coll) :running true :post true})]
    (doto pb
      (.setMaximum cnt)
      (.setIndeterminate false)
      (.setString (str (first coll)))
      (.setStringPainted true))
    (add-watch task-agent :task-agent
               (fn [k r o n]
                 (doto pb
                   (.setValue (:current n))
                   (.setString (str (:element n))))))
    (letfn [(task-fn
             [agent-val lst task-args]
             (if (and (:running agent-val)
                      (not (empty? lst)))
               (do
                 (let [{:keys [current element running] :or {current 1 running true element (first (rest lst))}}
                       (apply f agent-val (first lst) task-args)]
                   (send *agent* task-fn (rest lst) task-args)
                   (assoc (merge-with + agent-val {:current current}) :element element :running running)))
               (if (:post agent-val)
                 (apply f agent-val :nil task-args)
                 agent-val)))]
      (apply send task-agent task-fn coll args))
    [pb task-agent]))

(defn progress-bar
  [coll f & args]
  (let [cnt (count coll)
        pb (JProgressBar.)]
    (apply add-progress-listener pb coll f args)))

(defn indeterminate-progress-bar
  [pb [indeterminate-fn & indeterminate-args] [determinate-fn & determinate-args]]
  (let [indeterminate-agent (agent nil)]
    (doto pb
      (.setIndeterminate true)
      (.setString "wah")
      (.setStringPainted true))
    (send indeterminate-agent (fn [a args] (apply indeterminate-fn args)) indeterminate-args)
    (add-watch indeterminate-agent :indeterminate-agent
               (fn [k r o n]
                 (if (coll? n)
                   (add-progress-listener pb n determinate-fn determinate-args)
                   (doto pb
                     (.setString "wah")))))
    [pb indeterminate-agent]))
