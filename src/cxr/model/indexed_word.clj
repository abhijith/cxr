(ns cxr.model.indexed-word
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:use [cxr.db.config  :only (db-config)]))

(defn find
  [word]
  (find-record :indexed_word {:word word})) ; get rid of :indexed_word using meta data about table

(defn find-all
  []
  (qs {:from [:indexed_word]}))

(defn files
  [word]
  (map :name
       (qs {:cols [:indexed_file.name]
            :from [:indexed_file :indexed_word]
            :through :doc_index
            :and-where {:equal [[:indexed_word.word word]]}})))

(defn create
  [word]
  (create-record :indexed_word {:word word}))
