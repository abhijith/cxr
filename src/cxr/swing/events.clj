(ns cxr.swing.events
  (:import (javax.swing SwingUtilities))
  (:import (java.awt.event MouseAdapter ItemListener))
  (:require [cxr.swing.dialog :as dialog])
  (:use (clojure.contrib
         [swing-utils :only (make-menubar add-action-listener)])))

(defn double-click? [event]
  (= 2 (.getClickCount event)))

(defn add-mouse-listener
  [component]
  (let [listener (proxy [MouseAdapter] []
                   (mouseClicked
                    [event]
                    (and (double-click? event) (SwingUtilities/isLeftMouseButton event)
                         (dialog/debug (str (.getValueAt component (.getSelectedRow component) (.getSelectedColumn  component)))))))]
    (.addMouseListener component listener)
    listener))

(defn add-item-listener
  [component f & args]
  (let [listener (proxy [ItemListener] []
		   (itemStateChanged [event]
		     (apply f event args))) ]
    (.addItemListener component listener)))
