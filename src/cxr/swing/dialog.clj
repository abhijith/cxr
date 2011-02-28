(ns cxr.swing.dialog
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.search.core :as search])
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

;; progress bar code
(def pb (JProgressBar. 0 10))
(def pb-agent (agent 0)) ;; update progress of finding files for indexing using the progress as the value of this agent
(def determinate (agent nil))

(defn task
  [x lst start end]
  (if (and (deref globals/running) (not (= start end)))
    (do (send *agent* task (rest lst) (inc start) end)
      ;; essence can be pulled out of this function; apply f args or a macro (would work out better?)
        (let [fname (first lst)] ;; BACKEND functionality
          (search/index-file fname)
      (inc x)))
    (do (reset! globals/running false) end)))

;; change the mode of the progress bar from indeterminate to determinate and set the start and end values after find-files has finished
(defn init-determinate-agent-watch
  [] 
  (add-watch determinate
    :determinate
    (fn [k r o n]
      (let [ lst (deref *agent*) end (count lst) ]
	(.setIndeterminate pb false)
	(.setMaximum pb end) 
	(send pb-agent task lst 0 end)))))

(defn init-pb-agent-watch [] ;; set the pb-agent's value as the progress value when the value of pb-agent changes
  (add-watch pb-agent :pb-agent
    (fn [k r o n]
      (.setValue pb n) pb)))

;;; progressbar
;;; - set determinate mode or set start end
;;; - set progress value
;;; - fn to set start-end (switch to determinate mode)
;;; - fn & args to run and whose progress is to be monitored

(defn progress-bar
  [frame]
  (let [ panel (miglayout (JPanel.)
                          (JButton. "stop") { :id :stop })
        dialog (JDialog. frame) ]
    (.add panel pb)
    (.setIndeterminate pb true)
    (.setString pb "0")
    (.add panel (:stop (components panel)))
    (doto dialog
      (.add panel)
      (.setModal false)
      (.pack)
      (.setVisible true))
    (dosync (reset! globals/running true))
    (init-determinate-agent-watch)
    (init-pb-agent-watch)
    (add-action-listener (:stop (components panel)) (fn [_] (dosync (reset! globals/running false))))))

(defn ask-open
  [event frame]
  (let [chooser (JFileChooser.)]
    (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
    (let [ ret (.showOpenDialog chooser frame)]
      (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
      (cond
       (= JFileChooser/APPROVE_OPTION ret) (let [pb (progress-bar frame) dir (.getSelectedFile chooser)]
                                             (send determinate (fn [_] (search/find-files dir)
                                                                 (search/get-files)))) ;; backend
       (= JFileChooser/CANCEL_OPTION ret) (debug "canceleshwar")
       :else "error"))))
