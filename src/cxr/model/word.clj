(ns cxr.model.word
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:use [cxr.db.config  :only (db-config)])
  (:refer-clojure :exclude [find]))

(defn find
  [word]
  (find-record :word {:word word}))

(defn thesauri
  "all words from an indexed file"
  [word]
  (map :name (qs {:cols [:thes.name]
                  :from [:word :thes]
                  :through :contexts
                  :and-where {:equal [[:word.word word]] }})))

(defn create
  [word]
  (create-record :word {:word word}))
