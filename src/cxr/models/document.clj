(ns cxr.models.document
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.sqlwrap :only (qs select)])
  (:use [cxr.db.config  :only (db-config)]))

(defn find
  []
  [file word line offset]
  (qs {:cols [:indexed_word.word]
       :from [:indexed_word :indexed_file]
       :through :document
       :and-where {:equal [[:document.line line]
                           [:indexed_file.name name]]}}))

(defn find-all
  []
  (qs {:from [:document]}))

(defn line-nums
  [word]
  (with-connection db-config    
    (qs {:distinct true
         :cols [:document.line :indexed_file.name]
         :from [:indexed_word :indexed_file]
         :through :document
         :and-where {:equal [[:indexed_word.word word]]}})))

(defn line-words
  [{:keys [name line]}]
  (with-connection db-config    
    (map :word (qs {:cols [:indexed_word.word]
                    :from [:indexed_word :indexed_file]
                    :through :document
                    :and-where {:equal [[:document.line line]
                                        [:indexed_file.name name]]}}))))

(defn find-by-file
  [word file]
  (map line-words
       (with-connection db-config
         ;; should be a variation of line-nums => (line-nums [word file])
         (qs {:distinct true
              :cols [:document.line :indexed_file.name]
              :from [:indexed_word :indexed_file]
              :through :document
              :and-where {:equal [[:indexed_file.name file]
                                  [:indexed_word.word word]]}}))))

(defn find-all-context-words ;; find better name
  [word]
  (mapcat line-words (line-nos word)))

(defn insert
  [file word line offset]
  (insert-record :doc_index {:indexed_file_id (:id (indexed-file file))
                             :indexed_word_id (:id (indexed-word word))
                             :line line
                             :offset offset}))
