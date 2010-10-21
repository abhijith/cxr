(ns cxr.models.stop-word
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs select)])
  (:use [cxr.db.config  :only (db-config)]))

(defn find
  [word]
  (find-record :stop_word {:word word}))

(defn create
  [word]
  (create-record :stop_word {:word word}))

