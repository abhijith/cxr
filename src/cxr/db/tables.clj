(ns cxr.db.tables
  (:gen-class)
  (:use [cxr.db.config :only (db-config)])
  (:use [clojure.contrib.sql :as sql]))
  
(defn create-stop-word
  "words to be excluded during indexing"
  []
  (sql/create-table
   :stop_word
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(32)" "UNIQUE"]))

(defn create-indexed-file
  "information about a file"
  []
  (sql/create-table
   :indexed_file
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:name "varchar(256)" "UNIQUE"]
   [:md5 "varchar(256)" "UNIQUE"]
   [:indexed "boolean" "DEFAULT FALSE"]))

(defn create-indexed-word
  []
  (sql/create-table
   :indexed_word
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(100)" "UNIQUE"]))

(defn create-document
  "representation of document"
  []
  (sql/create-table
   :document
   [:indexed_word_id "BIGINT NOT NULL, FOREIGN KEY(indexed_word_id) REFERENCES indexed_word(id) ON DELETE CASCADE"]
   [:indexed_file_id "BIGINT NOT NULL, FOREIGN KEY(indexed_file_id) REFERENCES indexed_file(id) ON DELETE CASCADE"]
   [:line "INT"]
   [:offset "INT"]))

(defn create-thes
  "thesauri files"
  []
  (sql/create-table
   :thes
   [:id "int" "PRIMARY KEY AUTO_INCREMENT"]
   [:name "varchar(50)" "UNIQUE"]
   [:md5 "varchar(256)" "UNIQUE"]
   [:indexed "boolean" "DEFAULT FALSE"]))

(defn create-words
  "words present both in thesauri and documents"
  []
  (sql/create-table
   :word
   [:id "bigint" "PRIMARY KEY AUTO_INCREMENT"]
   [:word "varchar(50)" "UNIQUE"]))

(defn create-context
  "representation of a thesauri"
  []
  (sql/create-table
   :context
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
     (create-context)
     (create-indexed-file)
     (create-indexed-word)
     (create-document))))

(defn -main []
  (create-tables))
