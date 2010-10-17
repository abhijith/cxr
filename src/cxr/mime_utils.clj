(ns cxr.mime-utils
  (:import (org.apache.pdfbox.util PDFTextStripper))
  (:import (org.apache.pdfbox.pdmodel PDDocument))
  (:import (net.sf.jmimemagic Magic MagicMatchNotFoundException)))

(defn mime-type [path]
  (let [ f (java.io.File. path) ]
    (try 
      (. (Magic/getMagicMatch f true) getMimeType)
      (catch MagicMatchNotFoundException e nil))))

(defn pdf? [f]
  (= "application/pdf" (mime-type f)))

(defn extract-text [pdf-file txt-file]
  (let [stripper (PDFTextStripper.)]
    (with-open [ output (java.io.BufferedWriter. (java.io.FileWriter. txt-file))
                document (PDDocument/load pdf-file true) ]
      (try
        (. stripper writeText document output)
        (catch java.lang.NoClassDefFoundError e (println "gone" pdf-file))))))
