(ns cxr.model.stop-word
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:use [cxr.db.config  :only (db-config)]))

(defn find
  [word]
  (find-record :stop_word {:word word}))

(defn create
  [word]
  (create-record :stop_word {:word word}))

