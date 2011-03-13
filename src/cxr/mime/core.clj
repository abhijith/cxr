(ns cxr.mime.core
  (:import (net.sf.jmimemagic Magic MagicMatchNotFoundException)))

(defn mime-type
  [path]
  (let [ f (clojure.java.io/as-file path) ]
    (try 
      (. (Magic/getMagicMatch f true) getMimeType)
      (catch MagicMatchNotFoundException e nil))))

(defn pdf?
  [f]
  (re-matches #".*\.pdf$" f))

(defn text? 
  [f]
  (re-matches #".*\.txt$" f))
