(ns cxr.model.indexed-file
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:use [cxr.db.config  :only (db-config)]))

(defn find
  [name]
  (find-record :indexed_file {:name name}))

(defn find-all
  []
  (qs {:from [:indexed_file]}))

(defn words
  "all words from an indexed file"
  [file]
  (map :word (qs {:cols [:indexed_word.word]
                  :from [:indexed_word :indexed_file]
                  :through :document
                  :and-where {:equal [[:indexed_file.name file]] }})))

(defn create
  [name]
  (create-record :indexed_file {:name name}))
