(ns cxr.model.thes
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record update-cols)])
  (:use [cxr.db.config :only (db-config)])
  (:refer-clojure :exclude [find]))

(defn find
  [name]
  (find-record :thes {:name name}))

(defn find-all
  []
  (qs {:from [:thes]}))

(defn words
  "all words from an indexed file"
  ([thes]
     (map :word (qs {:cols [:word.word]
                     :from [:word :thes]
                     :through :context
                     :and-where {:equal [[:thes.name thes]] }})))
  ([]
     (map :word (qs {:cols [:word.word]
                     :from [:word :thes]
                     :through :context}))))

(defn create
  [name md5]
  (create-record :thes {:name name :md5 md5}))

(defn update
  [name indexed]
  (update-cols {:table :thes
                :and-where {:equal [[:name name]]}
                :set   [[:indexed indexed]]}))
