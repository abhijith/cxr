(ns cxr.db.sqlwrap
  (:use [clojure.contrib.sql :as sql])
  (:use [clj-sql.core :as clj-sql :only (insert-record)])
  (:use [clojure.contrib.string :only (join as-str substring?)]))

(defn squote
  [x]
  (if (string? x)
    (format (if (substring? "'" x) "$$%s$$" "'%s'") x)
    x))

(defn quotify
  [coll]
  (map (fn [x] (if (keyword? x) (as-str x) (squote x))) coll))

(defn likify
  [coll]
  (map (fn [x] (if (keyword? x) (as-str x) (squote (str "%" x "%")))) coll))

(defn cols
  [coll]
  (if (or (empty? coll) (nil? coll)) "*" (join ", " coll)))

(defn from
  [coll]
  (format "FROM %s" (join ", " coll)))

(defn dist
  [bool]
  (if bool "distinct"))

(defn setter
  [cols]
  (format "SET %s" (join "," (map (fn [x] (join " = " x)) (map quotify cols)))))

(defn and-where
  [h]
  (if (or (:equal h) (:like h))
    (format "WHERE %s"
            (join " AND "
                  (concat (map (fn [x] (join " = " x)) (map quotify (:equal h)))
                          (map (fn [x] (join " like " x)) (map likify (:like h)))))))) ;; refactor

(defn join-tables [h]
  (map (fn [table]
         [(keyword (as-str table "." (get h :using :id))) (keyword (as-str (:through h) "." table "_" (get h :using :id)))])
       (:tables h)))

(defn select-helper
  [data]
  (let [h (if (:through data)
            (let [fr (conj (:from data) (:through data))
                  e (join-tables (assoc data :tables (:from data)))
                  aw (update-in (:and-where data) [:equal] concat e) ]
              {:from fr :and-where aw :cols (:cols data)})
            data)]
    h))

(defn select ;; use format and clean up this nonsense
  [data]
  (let [h (select-helper data)
        q (join " " ["SELECT"
                     (dist (:distinct h))
                     (cols (quotify (:cols h)))
                     (from (quotify (:from h)))
                     (and-where (:and-where h))])]
    q))

(defn like ;; use format and clean up this nonsense
  [data]
  (let [h (select-helper data)
        q (join " " ["SELECT"
                     (dist (:distinct h))
                     (cols (quotify (:cols h)))
                     (from (quotify (:from h)))
                     (and-where (:and-where h))])]
    q))

(defn updater
  [q]
  (do-commands q))

(defn update-cols
  [data]
  (updater (format "UPDATE %s %s %s" (as-str (:table data)) (setter (:set data)) (and-where (:and-where data)))))


;; use delete rows
(defn delete ;; body is redundant
  [data]
  (let [f (from (quotify (:from data)))
        w (and-where (:and-where data)) ]
  (join " " ["delete" f w])))
  
(defn query-all
  [q]
  (sql/with-query-results rs [q]
    (into [] rs)))

(defn query
  [q]
  (sql/with-query-results rs [q]
    (let [[f r] (doall rs)] f)))

(defn qs
  [d]
  (query-all (select d)))

;;; generate fully qualified column name
(defn qual-name
  [table col]
  (keyword (as-str table "." col)))

(defn- find-helper
  [table rec]
  (map (fn [x] (let [[c v] (find rec x)] [(qual-name table c) v]))
       (keys rec)))

(defn find-record
  [table rec]
  (query (select {:from [table]
                  :and-where {:equal (find-helper table rec)}})))

(defn create-record
  [table rec]
  (let [h (find-record table rec)]
    (if (nil? h)
      (insert-record table rec)
      (:id h))))

(defn find-records 
  [table rec]
  (query-all (select {:from [table]
                      :and-where {:equal (find-helper table rec)}})))
