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
  (boolean (re-matches #".*\.pdf$" f)))

(defn text? 
  [f]
  (boolean (re-matches #".*\.txt$" f)))
