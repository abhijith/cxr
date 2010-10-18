(ns cxr.core
  (:gen-class)
  (:use [clojure.contrib.seq-utils :only (indexed)])
  (:use [clojure.contrib.io :only (read-lines)])
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.environment :only (db-config)])
  (:use [cxr.db :only (get-related-words)])
  (:use [clj-sql.core :only (insert-record)])
  (:use [cxr.sqlwrap :only (find-record create-record qs select)])
  (:use [cxr.tokenizer :as tokenizer])
  (:use [cxr.mime-utils :only (pdf? extract-text)]))


(defn stop-word?
  [word]
  (find-record :stop_word {:word word}))

(defn indexed-file
  [name]
  (find-record :indexed_file {:name name}))

(defn indexed-word
  [word]
  (find-record :indexed_word {:word word}))

(defn thes
  [name]
  (find-record :thes {:name name}))

(defn known-word
  [word]
  (find-record :word {:word word}))

(defn known-word?
  [word]
  (if (find-record :word {:word word}) true false))

(defn unknown-word?
  [word]
  (not (known-word? word)))

;; (insert-record indexed-file-obj indexed-word-object) preferred
(defn insert-into-doc-index
  [file word line offset]
  (insert-record :doc_index {:indexed_file_id (:id (indexed-file file))
                             :indexed_word_id (:id (indexed-word word))
                             :line line
                             :offset offset}))
(defn insert-into-related
  [thes-name word num offset]
  (insert-record :related {:thes_id (:id (thes thes-name))
                           :word_id (:id (known-word word))
                           :line num
                           :offset offset}))

(defn related-words
  [word]
  (if (known-word? word)
    (get-related-words word)))

(defn search-helper
  [word func]
  (frequencies (flatten (map keyword-search (func word)))))

(defn related-search
  [word]
  (search-helper word get-related-words))

(defn search
  [word]
  (frequencies (flatten (map keyword-search (related-words word)))))

(defn add-stop-words
  [f]
  (let [name (.getName (java.io.File. f))]
    (doseq [word (map sanitize (tokenizer/tokenize (slurp f)))]
      (create-record :stop_word {:word word}))))

(declare *stop-words*)

(defn load-stop-words
  []
  (def *stop-words* (ref {}))
  (doseq [rec (qs {:from [:stop_word]})]
    (dosync (alter *stop-words* assoc (:word rec) true))))

(defn stop-word?
  [word]
  (*stop-words* word))

(defn keyword-search
  [word]
  (map :name
       (qs {:cols [:indexed_file.name]
            :from [:indexed_file :indexed_word]
            :through :doc_index
            :and-where {:equal [[:indexed_word.word word]]}})))

;; split functionality into separate functions and rename the following functions
(defn tokenize-file
  [f]
  (map (fn [[line-num line]] [line-num (indexed (filter
                                                (fn [x] (and (word? x) (not (stop-word? x))))
                                                (map tokenizer/sanitize (tokenize line))))])
       (indexed (remove empty? (read-lines f)))))

(defn index-file
  [f]
  (let [fname (.getAbsolutePath (java.io.File. f))]
    (do (create-record :indexed_file {:name fname})
        (doseq [[line coll] (tokenize-file fname) [offset word] coll]
          (do (create-record :indexed_word {:word word})
              (insert-into-doc-index fname word line offset))))))

(defn index
  [dir]
  (sql/with-connection db-config
    (load-stop-words)
    (doseq [ [idx f] (indexed (filter (fn [x] (and (not (.isDirectory x)) (pdf? (.getAbsolutePath x)))) (file-seq dir))) ]
      (do (println idx (.getAbsolutePath f))
          (extract-text (.getAbsolutePath f) "/tmp/reaper.txt")
          (index-file (.getName f) :pdf true)))))

(defn add-thes
  [f]
  (let [fname (.getAbsolutePath (java.io.File. f))]
    (do (create-record :thes {:name fname})
        (doseq [[line coll] (tokenize-file fname) [offset word] coll]
          (do (create-record :word {:word word})
              (insert-into-related fname word line offset))))))
