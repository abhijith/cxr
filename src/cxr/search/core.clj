(ns cxr.search.core
  (:gen-class)
  (:use [clojure.contrib.seq-utils :only (indexed)])
  (:use [clojure.contrib.io :only (read-lines)])
  (:use [clojure.contrib.sql :as sql])
  (:use [cxr.db.config :only (db-config)])
  (:use [cxr.search.tokenizer :as tokenizer])
  (:use [cxr.mime.core :only (pdf?)])
  (:use [cxr.mime.pdf :only (to-text)])
  (:require [cxr.model.thes :as model.thes])
  (:require [cxr.model.word :as model.word])
  (:require [cxr.model.indexed-file :as model.indexed-file])
  (:require [cxr.model.indexed-word :as model.indexed-word])
  (:require [cxr.model.stop-word :as model.stop-word])
  (:require [cxr.model.document :as model.document])
  (:require [cxr.model.context :as model.context]))


(defn add-stop-words
  [f]
  (let [name (.getName (java.io.File. f))]
    (doseq [word (map sanitize (tokenizer/tokenize (slurp f)))]
      (model.stop-word/create word))))

(declare *stop-words*)

(defn load-stop-words
  []
  (def *stop-words* (ref {}))
  (doseq [rec (model.stop-word/find-all)]
    (dosync (alter *stop-words* assoc (:word rec) true))))

(defn known-word?
  [word]
  (if (model.word/find word) true false))

(defn indexed-word?
  [word]
  (if (model.indexed-word/find word) true false))

(defn indexed-file?
  [name]
  (if (model.indexed-file/find name) true false))

(defn stop-word?
  [word]
  (if (model.stop-word/find word) true false))

(defn stop-word?
  [word]
  (*stop-words* word))

(defn tokenize-file
  [f]
  (map (fn [[line-num line]]
         [line-num (indexed (filter (fn [x] (and (word? x) (not (stop-word? x)))) (map tokenizer/sanitize (tokenize line))))])
       (indexed (remove empty? (read-lines f)))))

(defn index-file
  [f]
  (let [fname (.getAbsolutePath (java.io.File. f))]
    (do (model.indexed-file/create fname)
        (doseq [[line coll] (tokenize-file fname) [offset word] coll]
          (do (model.indexed-word/create word)
              (model.document/insert fname word line offset))))))

(defn add-thes
  [f]
  (let [fname (.getAbsolutePath (java.io.File. f))]
    (do (model.thes/create fname)
        (doseq [[line coll] (tokenize-file fname) [offset word] coll]
          (do (model.word/create word)
              (model.context/insert fname word line offset))))))

(defn filename-search
  [name]
  (sql/with-connection db-config
    (model.indexed-file/find name)))

(defn keyword-search
  [word]
  (frequencies
   (sql/with-connection db-config
     (model.document/files word))))

(defn context-search
  [word]
  (sql/with-connection db-config
    (frequencies
     (mapcat model.document/files
             (if (known-word? word) (model.context/words word) (model.document/words word))))))
