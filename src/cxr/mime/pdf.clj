(ns cxr.mime.pdf
  (:use [cxr.mime.core :only (pdf?)])
  (:require [clojure.contrib.string :as string :only (replace-re)])
  (:require [clojure.contrib.logging :as log])
  (:import (org.apache.pdfbox.util PDFTextStripper))
  (:import (org.apache.pdfbox.pdmodel PDDocument)))

(defn to-text
  [pdf-file txt-file]
  (let [stripper (PDFTextStripper.)]
    (with-open [output (java.io.BufferedWriter. (java.io.FileWriter. txt-file))
                document (PDDocument/load pdf-file true) ]
      (try
        (. stripper writeText document output)
        (catch java.lang.NoClassDefFoundError e (log/error (str "to-text failed: " pdf-file)))))))

(defn convert
  [fname]
  (if (pdf? fname)
    (let [name (string/replace-re #"\.pdf$" ".txt" fname)]
      (to-text fname name)
      name)
    fname))
