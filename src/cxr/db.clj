;; http://en.wikibooks.org/wiki/Clojure_Programming/Examples/JDBC_Examples#MySQL_2
(ns cxr.db
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.sqlwrap :only (qs select)])
  (:use [cxr.environment :only (db-config)]))

(defn indexed-line-nums
  [word]
  (with-connection db-config
  (qs {:distinct true
       :cols [:doc_index.line :indexed_file.name]
       :from [:indexed_word :indexed_file]
       :through :doc_index
       :and-where {:equal [[:indexed_word.word word]]}})))

(defn indexed-line-words
  [{:keys [name line]}]
  (with-connection db-config    
    (map :word (qs {:cols [:indexed_word.word]
                    :from [:indexed_word :indexed_file]
                    :through :doc_index
                    :and-where {:equal [[:doc_index.line line]
                                        [:indexed_file.name name]]}}))))

(defn related-words-from-indexed
  "get words related to a word from a particular doc_index"
  [word name]
  (map indexed-line-words
       (with-connection db-config
         (qs {:distinct true
              :cols [:doc_index.line :indexed_file.name]
              :from [:indexed_word :indexed_file]
              :through :doc_index
              :and-where {:equal [[:indexed_file.name name]
                                  [:indexed_word.word word]]}}))))

(defn thes-line-nums
  [word]
  (with-connection db-config    
    (qs {:distinct true
         :cols [:related.line :thes.name]
         :from [:word :thes]
         :through :related
         :and-where {:equal [[:word.word word]]}})))

(defn thes-line-words
  [{:keys [name line]}]
  (with-connection db-config    
    (map :word (qs {:cols [:word.word]
                       :from [:word :thes]
                       :through :related
                       :and-where {:equal [[:related.line line]
                                           [:thes.name name]]}}))))

(defn related-words-from-thes
  "get words related to a word from a particular thes"
  [word thes]
  (map thes-line-words
       (with-connection db-config
         (qs {:distinct true
              :cols [:related.line :thes.name]
              :from [:word :thes]
              :through :related
              :and-where {:equal [[:thes.name thes]
                                  [:word.word word]]}}))))

(defn indexed-words
  "all words from an indexed file"
  [file]
  (map :word (qs {:cols [:indexed_word.word]
                  :from [:indexed_word :indexed_file]
                  :through :doc_index
                  :and-where {:equal [[:indexed_file.name file]] }})))

(defn doc-index-to-word-mapping
  "get mapping between file and words"
  []
  (qs {:cols [:indexed_word.word :indexed_file.name]
       :from [:indexed_file :indexed_word]
       :through :doc_index }))

(defn show-columns
  [table]
  (sql/with-connection db-config
    (sql/with-query-results rs [(str "show columns from " table)]
      (map :field (doall rs)))))

(defn thes-to-word-mapping
  "get mapping between word and thes"
  []
  (qs {:cols [:word.word :thes.name]
       :from [:thes :word]
       :through :related }))

(defn get-known-words-from-file
  "all words from a thes"
  [file]
  (map :word (qs {:cols [:word.word]
                  :from [:word :thes]
                  :through :related
                  :and-where {:equal [[:thes.name file]]}})))

(defn get-indexed-words-from-file
  "all words from a indexed file"
  [file]
  (map :word (qs {:cols [:indexed_word.word]
                  :from [:indexed_word :indexed_file]
                  :through :doc_index
                  :and-where {:equal [[:indexed_file.name file]] }})))
