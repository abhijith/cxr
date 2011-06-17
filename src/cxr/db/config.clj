(ns cxr.db.config
  (:import (System)))

(let [db-protocol "file"
      db-host     (System/getProperty "user.home")
      db-name     ".cxr"]
  (def db-config {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname (str db-protocol "://" db-host "/" db-name)
                  :user     "cxr"
                  :password "cxr"})
  (def db-path (str db-host "/" db-name ".h2.db")))

(let [db-protocol "file"
      db-host     "/tmp"
      db-name     "test"]
    (def test-db {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname (str db-protocol "://" db-host "/" db-name)
                  :user     "test"
                  :password "test"}))
