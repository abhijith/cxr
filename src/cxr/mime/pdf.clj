(ns cxr.mime.pdf
  (:import (org.apache.pdfbox.util PDFTextStripper))
  (:import (org.apache.pdfbox.pdmodel PDDocument))
  
(defn to-text [pdf-file txt-file]
  (let [stripper (PDFTextStripper.)]
    (with-open [ output (java.io.BufferedWriter. (java.io.FileWriter. txt-file))
                document (PDDocument/load pdf-file true) ]
      (try
        (. stripper writeText document output)
        (catch java.lang.NoClassDefFoundError e (println "gone" pdf-file))))))
