(ns cxr.model.context
  (:use [clojure.contrib.sql :as sql])
  (:use [clj-sql.core :as clj-sql :only (insert-record)])
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:use [cxr.db.config :only (db-config)])
  (:require [cxr.model.thes :as model.thes])
  (:require [cxr.model.word :as model.word]))

(defn find
  [thes word line offset]
  (qs {:cols [:thes.name]
       :from [:word :thes]
       :through :context
       :and-where {:equal [[:context.line line]
                           [:thes.name thes]]}}))

(defn find-all
  []
  (qs {:from [:context]}))

(defn line-nos
  [word]
  (with-connection db-config    
    (qs {:distinct true
         :cols [:context.line :thes.name]
         :from [:word :thes]
         :through :context
         :and-where {:equal [[:word.word word]]}})))

(defn line-words
  [{:keys [name line]}]
  (with-connection db-config    
    (map :word (qs {:cols [:word.word]
                    :from [:word :thes]
                    :through :context
                    :and-where {:equal [[:context.line line]
                                        [:thes.name name]]}}))))

(defn words-by-thes
  [word thes]
  (map line-words
       (with-connection db-config
         ;; should be a variation of line-nums => (line-nums [word thes])
         (qs {:distinct true
              :cols [:context.line :thes.name]
              :from [:word :thes]
              :through :context
              :and-where {:equal [[:thes.name thes]
                                  [:word.word word]]}}))))

(defn words
  [word]
  (mapcat line-words (line-nos word)))

(defn thesauri
  [word]
  (with-connection db-config
    (qs {:distinct true
         :cols [:context.line :thes.name]
         :from [:word :thes]
         :through :context
         :and-where {:equal [[:word.word word]]}})))

(defn insert
  [thes-name word num offset]
  (insert-record :context {:thes_id (:id (model.thes/find thes-name))
                           :word_id (:id (model.word/find word))
                           :line num
                           :offset offset}))
