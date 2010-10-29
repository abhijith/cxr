(ns cxr.swing.menu
  (:import (java.awt.event MouseAdapter KeyEvent ItemListener ItemEvent))
  (:use (clojure.contrib
         [swing-utils :only (make-menubar)]))
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
