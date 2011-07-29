(ns cxr.swing.events
  (:gen-class)
  (:import (javax.swing SwingUtilities))
  (:import (java.awt.event MouseAdapter ItemListener))
  (:require [cxr.swing.dialog :as dialog])
  (:use (clojure.contrib
         [swing-utils :only (make-menubar add-action-listener)] [shell-out :only (sh) :as sh])))

(defn double-click? [event]
  (= 2 (.getClickCount event)))

(defn add-mouse-listener
  [component]
  (let [listener (proxy [MouseAdapter] []
                   (mouseClicked
                    [event]
                    (if (and (double-click? event) (SwingUtilities/isLeftMouseButton event))
                      (sh/sh "/usr/bin/xdg-open" (.getValueAt component (.getSelectedRow component) (.getSelectedColumn  component))))))]
    (.addMouseListener component listener)
    listener))

(defn add-item-listener
  [component f & args]
  (let [listener (proxy [ItemListener] []
		   (itemStateChanged [event]
		     (apply f event args))) ]
    (.addItemListener component listener)))
