(ns cxr.swing.menu
  (:import (java.awt.event KeyEvent))
  (:use (clojure.contrib
         [miglayout :only (miglayout components)]
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
                    :handler (fn [_] (dialog/ask-open-dir nil frame)) }]}
                 
                 {:name     "foobar"
                  :mnemonic KeyEvent/VK_F
                  :items
                  [{:name "Add thesaurus"
                    :mnemonic KeyEvent/VK_T
                    :short-desc ""
                    :long-desc ""
                    :handler (fn [_] (dialog/ask-open-file nil frame cxr.search.core/add-thes)) }
                   {:name "Add stop words"
                    :mnemonic KeyEvent/VK_S
                    :short-desc ""
                    :long-desc ""
                    :handler (fn [_] (dialog/ask-open-file nil frame cxr.search.core/add-stop-words)) }]}]))
