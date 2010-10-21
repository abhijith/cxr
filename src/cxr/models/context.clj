(ns cxr.models.context
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs select)])
  (:use [cxr.db.config :only (db-config)]))

(defn find-all
  []
  (qs {:from [:document]}))

(defn line-nums
  [word]
  (with-connection db-config    
    (qs {:distinct true
         :cols [:related.line :thes.name]
         :from [:word :thes]
         :through :related
         :and-where {:equal [[:word.word word]]}})))

(defn line-words
  [{:keys [name line]}]
  (with-connection db-config    
    (map :word (qs {:cols [:word.word]
                       :from [:word :thes]
                       :through :related
                       :and-where {:equal [[:related.line line]
                                           [:thes.name name]]}}))))

(defn find-by-thes
  "get words related to a word from a particular thes"
  [word thes]
  (map line-words
       (with-connection db-config
         ;; should be a variation of line-nums => (line-nums [word thes])
         (qs {:distinct true
              :cols [:related.line :thes.name]
              :from [:word :thes]
              :through :related
              :and-where {:equal [[:thes.name thes]
                                  [:word.word word]]}}))))

(defn find-all-contexts-words ;; find better name 
  [word]
  (mapcat line-words (line-nos word)))

(defn insert
  [thes-name word num offset]
  (insert-record :related {:thes_id (:id (thes thes-name))
                           :word_id (:id (known-word word))
                           :line num
                           :offset offset}))
