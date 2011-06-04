(ns cxr.search.core
  (:gen-class)
  (:require (clojure.contrib
             [seq-utils :only (indexed) :as seq-utils ] [io :only (read-lines) :as io] [sql :as sql] [string :only (replace-re) :as string]))
  (:use [cxr.db.config :only (db-config)])
  (:use [cxr.search.tokenizer :as tokenizer])
  (:use (cxr.mime [core :only (pdf? text?)] [pdf :only (to-text convert)]))
  (:use [cxr.db.sqlwrap :only (qs find-record create-record)])
  (:import (java.security MessageDigest Security))
  (:require (cxr.model
             [thes :as model.thes] [word :as model.word]
             [indexed-file :as model.indexed-file] [indexed-word :as model.indexed-word]
             [stop-word :as model.stop-word] [document :as model.document] [context :as model.context])))


(defn add-stop-words
  [f]
  (sql/with-connection db-config
    (doseq [word (map sanitize (tokenizer/tokenize (slurp f)))]
      (model.stop-word/create word))))

(def *stop-words* (ref {}))

(defn load-stop-words
  []
  (sql/with-connection db-config
    (dosync 
     (doseq [rec (model.stop-word/find-all)]
       (alter *stop-words* assoc (:word rec) true)))))

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
  (seq-utils/indexed (filter-toks (map tokenizer/sanitize (tokenize s)))))

(defn prepare-file
  [f]
  (map (fn [[line-num line]] [line-num (prepare-toks line)])
       (seq-utils/indexed (remove empty? (io/read-lines f)))))

(defn index-file
  [f]
  (sql/with-connection db-config
    (let [fname (.getAbsolutePath (java.io.File. f))
          out (if (pdf? fname) (convert fname) fname)]
      (model.indexed-file/create fname)
      (doseq [[line coll] (prepare-file out) [offset word] coll]
        (do (model.indexed-word/create word)
            (model.document/insert fname word line offset)
            (cxr.model.indexed-file/update fname true))))))

;; move this into utils
(defn md5
  "Compute the hex MD5 sum of a string."
  [str]
  (let [alg (doto (MessageDigest/getInstance "MD5")
              (.reset)
              (.update (.getBytes str)))]
    (.toString (new BigInteger 1 (.digest alg)) 16)))

(defn file-indexed?
  [x]
  (sql/with-connection db-config
    (model.indexed-file/find (.getAbsolutePath x))))

(defn file-modified?
  [x]
  (sql/with-connection db-config
    (and (model.indexed-file/find (.getAbsolutePath x))
         (model.indexed-file/find-by-md5 (md5 (.getAbsolutePath x))))))

(defn file-duplicate?
  [x]
  (sql/with-connection db-config
    (and (not (file-indexed? x)) (model.indexed-file/find-by-md5 (md5 (.getAbsolutePath x))))))

(defn list-files
  [dir]
  (filter (fn [x] (and (.isFile x)
                      (or (text? (.getAbsolutePath x)) (pdf? (.getAbsolutePath x))) x))
          (file-seq (clojure.java.io/as-file (clojure.java.io/as-file dir)))))

(defn find-thesauri
  [dir]
  (sql/with-connection db-config
    (doseq [fname (list-files dir) ]
      (create-record :thes {:name (.getAbsolutePath fname) :md5 (md5 (.getAbsolutePath fname))}))))

(defn find-files
  [dir]
  (sql/with-connection db-config
    (doseq [fname (list-files dir) ]
      (create-record :indexed_file {:name (.getAbsolutePath fname) :md5 (md5 (.getAbsolutePath fname))})))) ;; refactor - build a multi-method style create or use keyword args

(defn get-files
  []
  (filter (fn [x] (= (:indexed x) false))
          (sql/with-connection db-config
            (model.indexed-file/find-all))))

(defn get-thesauri
  []
  (filter (fn [x] (= (:indexed x) false))
          (sql/with-connection db-config
            (model.thes/find-all))))

(defn add-thes
  [f]
  (sql/with-connection db-config
    (let [fname (.getAbsolutePath (java.io.File. f))]
      (do (model.thes/create fname (md5 fname))
          (doseq [[line coll] (prepare-file fname) [offset word] coll]
            (do (model.word/create word)
                (model.context/insert fname word line offset)
                (cxr.model.thes/update fname true)))))))

(defn- sort-result
  [res]
  (sort-by (fn [[rec wt]] wt) > res))

(defn- search [f arg]
  (sort-result 
   (frequencies
    (sql/with-connection db-config (f arg)))))


(defn filename-search
  [name]
  (search model.indexed-file/like name))

(defn keyword-search
  [word]
  (search model.document/files word))

(defn context-search
  [word]
  (search (fn [w] (mapcat model.document/files (model.context/words w))) word))
