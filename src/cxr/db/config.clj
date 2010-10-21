(ns cxr.db.config)

(let [db-protocol "file"
      db-host     "/tmp"
      db-name     "cxr"]
  (def db-config {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname (str db-protocol "://" db-host "/" db-name)
                  :user     "test"
                  :password "test"}))

(let [db-protocol "file"
      db-host     "/tmp"
      db-name     "test"]
    (def test-db {:classname   "org.h2.Driver"
                  :subprotocol "h2"
                  :subname (str db-protocol "://" db-host "/" db-name)
                  :user     "test"
                  :password "test"}))
