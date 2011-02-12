(ns cxr.swing.menu
  (:import (java.awt.event KeyEvent))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
         [swing-utils :only (make-menubar add-action-listener)]))
  (:require [cxr.swing.dialog :as dialog]))

(defn menubar
  [frame]
  (make-menubar [{:name     "Index"
                  :mnemonic KeyEvent/VK_I
                  :items
                  [{:name "Open Directory"
                    :mnemonic KeyEvent/VK_A
                    :short-desc ""
                    :long-desc ""
                    :handler (fn [_] (dialog/ask-open nil frame)) }] }]))
