(ns cxr.swing.utils
  (:import (javax.swing JButton JFrame JPanel JTextField JOptionPane JScrollPane JComboBox JTable JFileChooser JProgressBar JDialog SwingUtilities))
  (:import (javax.swing.table AbstractTableModel))
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:import (java.awt Toolkit Dimension Color))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)])))

;; util
(defn double-click? [event]
  (= 2 (.getClickCount event)))

(def msg-type {:info   JOptionPane/INFORMATION_MESSAGE
               :error   JOptionPane/ERROR_MESSAGE
               :warn    JOptionPane/WARNING_MESSAGE
               :message JOptionPane/PLAIN_MESSAGE })

(defn- dialog
  [parent msg type]
  (JOptionPane/showMessageDialog parent msg type (msg-type (keyword type))))

(defn info [parent msg]
  (dialog parent msg "info"))

;; cxr global
(def running (atom true))

;; globals for search button 
(def search-types { " keyword search" (fn [x] (find x true))  " filename search" find " context search" find })
(def search-fn (search-types " context search"))

;; search button handler
(defn show-results [a lst]
  (if (and @running (not-empty lst))
    (do (send *agent* show-results (rest lst))
      (conj @result-agent [(first lst)]))
    @result-agent))

(defn populate-table
  [event x]
  (let [word (.getText x)]
    (if-not (empty? word)
      (do
        (dosync (reset! running true))
          (send result-agent show-results (lazy-seq (search-fn word)))))))

(def table-model
  (proxy [AbstractTableModel] []
    (getColumnCount [] (count column-names))
    (getRowCount [] (count @result-agent))
    (getValueAt [i j] (get-in @result-agent [i j]))
    (getColumnName [i] (column-names i))))

(defn init-result-agent-watch []
  (do 
    (add-watch result-agent :result-agent
      (fn [k r o n]
	(.fireTableRowsInserted table-model 0 0)))))

;; abort button handler
(defn clear-table [event]
  (do
    (dosync (reset! running false))
    (send result-agent (fn [x] []))
    (.fireTableRowsDeleted table-model 0 0)))

;; combo button handler
(defn combo-handler
  [event]
  (let [ state-changed (= (.getStateChange event) ItemEvent/SELECTED) ]
    (if state-changed
      (def search-fn (search-types (str (.getItem event)))))))

;; index menu handler
;; progress bar code
(def pb (JProgressBar. 0 10))
(def pb-agent (agent 0)) ;; update progress of finding files for indexing using the progress as the value of this agent
(def finder (agent nil)) ;; index menu action dispatched to this agent

;; (defn move
;;   [x lst i end]
;;   (if (and @running (not (= i end)))
;;     (do (send *agent* move (rest lst) (inc i) end)
;;       ;; essence can be pulled out of this function; apply f args
;;       (let [pdf (:path (fetch-value file-index (first lst)))] ;; BACKEND functionality
;; 	(index pdf)) ;; BACKEND functionality
;;       (inc x))
;;     (do (reset! running false) end)))

;; (defn init-finder-agent-watch [] ;; change the mode of the progress bar from indeterminate to determinate and set the start and end values after find-files has finished
;;   (add-watch finder
;;     :finder
;;     (fn [k r o n]
;;       (let [ lst (with-cabinet file-index (primary-keys)) end (count lst) ] ;; BACKEND functionality ; user get-files
;; 	(.setIndeterminate pb false)
;; 	(.setMaximum pb end) ;; determine total num of files and set progress bar length (start & end)
;; 	(send pb-agent move lst 0 end)))))

(defn init-pb-agent-watch [] ;; set the pb-agent's value as the progress value when the value of pb-agent changes
  (add-watch pb-agent :pb-agent
    (fn [k r o n]
      (.setValue pb n) pb)))

(defn progress-bar
  [frame f & args]
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
    (dosync (reset! running true)) 
    (add-action-listener (:stop (components panel)) (fn [_] (dosync (reset! running false))))
    (init-pb-agent-watch)
    (init-finder-agent-watch)
    (apply f args)))

;; (defn show-open-dialog
;;   [event frame]
;;   (let [chooser (JFileChooser.)]
;;     (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
;;     (let [ ret (.showOpenDialog chooser frame)]
;;       (.setFileSelectionMode chooser JFileChooser/DIRECTORIES_ONLY)
;;       (cond
;; 	(= JFileChooser/APPROVE_OPTION ret) (progress-bar frame send-off finder (fn [_] (find-files (.getSelectedFile chooser)))) ;; BACKEND functionality
;; 	(= JFileChooser/CANCEL_OPTION ret) (info (JFrame.) "canceleshwar")
;; 	:else "error"))))

(defn menubar
  [frame]
  (make-menubar [{:name     "Index"
		   :mnemonic KeyEvent/VK_I
		   :items
		   [{:name "Open Directory" :mnemonic KeyEvent/VK_A :short-desc "" :long-desc "" :handler (fn [_] (show-open-dialog nil frame)) }] }]))

;; listeners
(defn add-mouse-listener
  [component]
  (let [listener (proxy [MouseAdapter] []
                   (mouseClicked
                    [event]
                    (and (double-click? event) (SwingUtilities/isLeftMouseButton event)
                         (info (JPanel.) (str (.getValueAt component (.getSelectedRow component) (.getSelectedColumn  component)))))))]
    (.addMouseListener component listener)
    listener))

(defn add-item-listener
  [component f & args]
  (let [listener (proxy [ItemListener] []
		   (itemStateChanged [event]
		     (apply f event args))) ]
    (.addItemListener component listener)))

(init-result-agent-watch)

(add-action-listener search-button populate-table search-box)
(add-action-listener abort-button clear-table)
(add-item-listener combo combo-handler)
(add-mouse-listener jtable)
