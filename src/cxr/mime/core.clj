(ns cxr.mime.core
  (:import (net.sf.jmimemagic Magic MagicMatchNotFoundException)))

(defn mime-type [path]
  (let [ f (java.io.File. path) ]
    (try 
      (. (Magic/getMagicMatch f true) getMimeType)
      (catch MagicMatchNotFoundException e nil))))

(defn pdf? [f]
  (= "application/pdf" (mime-type f)))
