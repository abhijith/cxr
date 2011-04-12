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
                  :through :context
                  :and-where {:equal [[:word.word word]] }})))

(defn create
  [word]
  (create-record :word {:word word}))

(defn line-nos
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

(defn words-by-file
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

(defn words
  [word]
  (mapcat line-words (line-nos word)))

(defn files
  [word]
  (with-connection db-config
    (qs {:distinct true
         :cols [:indexed_file.name]
         :from [:indexed_word :indexed_file]
         :through :document
         :and-where {:equal [[:indexed_word.word word]]}})))

(defn insert
  [file word line offset]
  (insert-record :document {:indexed_file_id (:id (model.indexed-file/find file))
                            :indexed_word_id (:id (model.indexed-word/find word))
                            :line line
                            :offset offset}))
