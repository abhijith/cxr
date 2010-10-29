(ns cxr.search.core
  (:gen-class)
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.config :only (db-config)])
  (:require [cxr.model.indexed-file :as model.indexed-file])
  (:require [cxr.model.indexed-word :as model.indexed-word])
  (:require [cxr.model.stop-word :as model.stop-word])
  (:require [cxr.model.document :as model.document])
  (:require [cxr.model.context :as model.context])
  (:refer-clojure :exclude [keyword]))

(defn filename
  [name]
  (sql/with-connection db-config
    (model.indexed-file/find name)))

(defn keyword
  [word]
  (frequencies
   (sql/with-connection db-config
     (model.document/files word))))

(defn context
  [word]
  (sql/with-connection db-config
    (frequencies
     (mapcat model.document/files
             (if (known-word? word) (model.context/words word) (model.document/words word))))))
