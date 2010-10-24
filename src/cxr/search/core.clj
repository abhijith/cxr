(ns cxr.search.core
  (:gen-class)
  (:use [clojure.contrib.seq-utils :only (indexed)])
  (:use [clojure.contrib.io :only (read-lines)])
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.config :only (db-config)])
  (:use [clj-sql.core :only (insert-record)])
  (:use [cxr.db.sqlwrap :only (find-record create-record qs select)])
  (:use [cxr.search.tokenizer :as tokenizer])
  (:require [cxr.model.thes :as model.thes])
  (:require [cxr.model.word :as model.word])
  (:require [cxr.model.indexed-file :as model.indexed-file])
  (:require [cxr.model.indexed-word :as model.indexed-word])
  (:require [cxr.model.stop-word :as model.stop-word])
  (:require [cxr.model.document :as model.document])
  (:require [cxr.model.context :as model.context])
  (:use [cxr.mime.core :only (pdf?)])
  (:use [cxr.mime.pdf :only (to-text)]))

(defn known-word?
  [word]
  (if (model.word/find word) true false))

(defn indexed-word?
  [word]
  (if (model.indexed-word/find word) true false))

(defn indexed-file?
  [name]
  (if (model.indexed-file/find name) true false))

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
  (if (model.stop-word/find word) true false))

(defn stop-word?
  [word]
  (*stop-words* word))

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
              (model.document/insert fname word line offset))))))

(defn index
  [dir]
  (sql/with-connection db-config
    (load-stop-words)
    (doseq [ [idx f] (indexed (filter (fn [x] (and (not (.isDirectory x)) (pdf? (.getAbsolutePath x)))) (file-seq dir))) ]
      (do (println idx (.getAbsolutePath f))
          (to-text (.getAbsolutePath f) "/tmp/reaper.txt")
          (index-file (.getName f) :pdf true)))))

(defn add-thes
  [f]
  (let [fname (.getAbsolutePath (java.io.File. f))]
    (do (create-record :thes {:name fname})
        (doseq [[line coll] (tokenize-file fname) [offset word] coll]
          (do (create-record :word {:word word})
              (model.context/insert fname word line offset))))))
