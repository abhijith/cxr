(ns cxr.test.core
  (:use [cxr.core] :reload)
  (:use [clojure.test]))

(deftest replace-me
  (is false "No tests have been written."))

;; assert the following and and more tests
(defn create-tables
  []
  (sql/with-connection db
    (sql/transaction
     (create-stop-word)
     (create-thes)
     (create-words)
     (create-related)
     (create-indexed-file)
     (create-indexed-word)
     (create-doc-index))))

(defn init-tables
  []
  (sql/with-connection db
    (sql/transaction
     (do 
       (insert-rows-thes)
       (insert-rows-word)
       (insert-rows-related)
       (insert-rows-indexed-file)
       (insert-rows-indexed-word)
       (insert-rows-doc-index)))))

(defn insert-rows-word
  "Insert complete rows"
  []
  (sql/insert-records
   :word
   {:word "lisp"}
   {:word "clojure"}
   {:word "lambda"}
   {:word "calculus"}
   {:word "Nietzsche"}
   {:word "Whitehead"}
   {:word "Buddha"}))

(defn insert-rows-indexed-file
  "Insert complete rows"
  []
  (sql/insert-records
   :indexed_file
   {:name "lisp.txt"}
   {:name "clojure.txt"}))

(defn insert-rows-indexed-word
  "Insert complete rows"
  []
  (sql/insert-records
   :indexed_word
   {:word "McCarthy"}
   {:word "lisp"}
   {:word "lambda"}
   {:word "calculus"}
   {:word "clojure"}
   {:word "Hickey"}
   {:word "egal"}
   {:word "Whitehead"}))

(defn insert-rows-doc-index
  []
  (sql/insert-rows
   :doc_index
   [1 1]
   [2 1]
   [1 1]
   [3 1]
   [4 1]
   [5 2]
   [2 2]
   [6 2]
   [5 2]
   [7 2]
   [8 2]))

(defn insert-into-doc-index
  [file word num]
  (sql-wrap/create-record :doc_index {:indexed_file_id (:id (indexed-file file))
                                      :indexed_word_id (:id (indexed-word word))}))

(defn insert-rows-thes
  "Insert complete rows"
  []
  (sql/insert-records
   :thes
   {:name "tech"}
   {:name "philo"}))

(defn insert-rows-word
  "Insert complete rows"
  []
  (sql/insert-records
   :word
   {:word "lisp"}
   {:word "clojure"}
   {:word "lambda"}
   {:word "calculus"}
   {:word "Nietzsche"}
   {:word "Whitehead"}
   {:word "Buddha"}))

(defn insert-rows-related
  []
  (sql/insert-rows
   :related
   [1 1 1]
   [2 1 1]
   [3 1 1]
   [3 1 2]
   [4 1 2]
   [5 2 1]
   [6 2 1]
   [7 2 1]))

