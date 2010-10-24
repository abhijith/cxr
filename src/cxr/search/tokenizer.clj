(ns cxr.search.tokenizer
  (:use [clojure.contrib.string :only (split trim lower-case chomp chop)])
  (:use [clojure.contrib.str-utils :only (re-gsub)]))

(defn sanitize
  [s]
  (re-gsub #"[^\w\-\d\ ]+" "" (re-gsub #"\/" " " (chomp (lower-case s)))))

(defn tokenize
  [s & {:keys [re] :or {re #"[\s,\.]"}}]
  (map #(trim (if (re-matches #".*-$" %1) (chop %1) %1)) (split re s)))

(defn word?
  [s]
  (if-let [word (re-matches #"^[a-zA-Z\-0-9\_]+$" s)]
    (if (and (> (count word) 2) (< (count word) 30)) word)))
