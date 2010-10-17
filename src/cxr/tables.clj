(ns cxr.tables
  (:use [cxr.environment :only (db-config)])
  (:use [clojure.contrib.sql :as sql]))
  
(defn create-stop-word
  "Create a table"
  []
  (sql/create-table
   :stop_word
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(32)" "UNIQUE"]))

(defn create-indexed-file
  []
  (sql/create-table
   :indexed_file
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:name "varchar(256)" "UNIQUE"]
   [:md5 "varchar(256)" "UNIQUE"]
   [:indexed "boolean" "DEFAULT TRUE"]))

(defn create-indexed-word
  []
  (sql/create-table
   :indexed_word
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(100)" "UNIQUE"]))

(defn create-doc-index
  []
  (sql/create-table
   :doc_index
   [:indexed_word_id "BIGINT NOT NULL, FOREIGN KEY(indexed_word_id) REFERENCES indexed_word(id) ON DELETE CASCADE"]
   [:indexed_file_id "BIGINT NOT NULL, FOREIGN KEY(indexed_file_id) REFERENCES indexed_file(id) ON DELETE CASCADE"]
   [:line "INT"]
   [:offset "INT"]))

(defn create-thes
  "Create a table"
  []
  (sql/create-table
   :thes
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:name "varchar(50)" "UNIQUE"]))

(defn create-words
  "Create a table"
  []
  (sql/create-table
   :word
   [:id "bigint" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(50)" "UNIQUE"]))

(defn create-related
  []
  (sql/create-table
   :related
   [:word_id "int NOT NULL, FOREIGN KEY(word_id) REFERENCES word(id)"]
   [:thes_id "int NOT NULL, FOREIGN KEY(thes_id) REFERENCES thes(id)"]
   [:line    "int"]
   [:offset  "int"]))

(defn create-tables
  []
  (sql/with-connection db-config
    (sql/transaction
     (create-stop-word)
     (create-thes)
     (create-words)
     (create-related)
     (create-indexed-file)
     (create-indexed-word)
     (create-doc-index))))
