(ns cxr.search.core
  (:gen-class)
  (:use (clojure.contrib
         [seq-utils :only (indexed)] [io :only (read-lines)] [sql :as sql]))
  (:use [cxr.db.config :only (db-config)])
  (:use [cxr.search.tokenizer :as tokenizer])
  (:use (cxr.mime [core :only (pdf? text?)] [pdf :only (to-text)]))
  (:require (cxr.model
             [thes :as model.thes] [word :as model.word]
             [indexed-file :as model.indexed-file] [indexed-word :as model.indexed-word]
             [stop-word :as model.stop-word] [document :as model.document] [context :as model.context])))


(defn add-stop-words
  [f]
  (with-connection db-config
    (let [name (.getName (java.io.File. f))]
      (doseq [word (map sanitize (tokenizer/tokenize (slurp f)))]
        (model.stop-word/create word)))))

(declare *stop-words*)

(defn load-stop-words
  []
  (with-connection db-config
  (def *stop-words* (ref {}))
  (doseq [rec (model.stop-word/find-all)]
    (dosync (alter *stop-words* assoc (:word rec) true)))))

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

(defn filter-toks
  [coll]
  (filter #(and (word? %1) (not (stop-word? %1))) coll))

(defn prepare-toks
  [s]
  (indexed (filter-toks (map tokenizer/sanitize (tokenize s)))))

(defn prepare-file
  [f]
  (map (fn [[line-num line]] [line-num (prepare-toks line)])
       (indexed (remove empty? (read-lines f)))))

(defn index-file
  [f]
  (with-connection db-config
    (let [fname (.getAbsolutePath (java.io.File. f))]
      (do (model.indexed-file/create fname)
          (doseq [[line coll] (prepare-file fname) [offset word] coll]
            (do (model.indexed-word/create word)
                (model.document/insert fname word line offset)))))))

(defn find-files
  [dir]
  (with-connection db-config
    (doseq [fname (file-seq (clojure.java.io/as-file dir)) ]
      (if (and (not (.isDirectory fname)) (text? (.getAbsolutePath fname)))
        (model.indexed-file/create (.getAbsolutePath fname))))))

(defn add-thes
  [f]
  (with-connection db-config
    (let [fname (.getAbsolutePath (java.io.File. f))]
      (do (model.thes/create fname)
          (doseq [[line coll] (prepare-file fname) [offset word] coll]
            (do (model.word/create word)
                (model.context/insert fname word line offset)))))))

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
