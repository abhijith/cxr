(ns cxr.test.core
  (:use [clojure.test])
  (:require [clojure.contrib.sql :as sql])
  (:use [cxr.db.tables :as tables])
  (:use [cxr.db.config :only (db-config)])
  (:require [cxr.search.core :as cxr] :reload))

(defn setup-db
  [f]
  (tables/create-tables)
  (f))

(defn add-thesauri
  []
  (cxr/add-thes "resources/testdata/thes/tech")
  (cxr/add-thes "resources/testdata/thes/philo"))

(use-fixtures :each setup-db)

(deftest thesauri
  []
  (add-thesauri)
  (is (= 2 (count (sql/with-connection db-config (cxr.model.thes/find-all))))))
