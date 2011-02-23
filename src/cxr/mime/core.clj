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
  (= "application/pdf" (mime-type f)))

(defn text? 
  [f]
  (= "text/plain" (mime-type f)))
