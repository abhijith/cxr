(defproject cxr "0.1"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [clj-sql "0.0.3"]
                 [com.h2database/h2 "1.2.140"]
                 [jmimemagic/jmimemagic "0.1.2"]
                 [org/apache/pdfbox/pdfbox "1.2.1"]
                 [oro/oro "2.0.8"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]
                 [com/miglayout/miglayout "3.7.1"]]
  :dev-dependencies [[swank-clojure "1.2.1"]]
  :aot [cxr.db.tables cxr.swing.core]
  :main cxr.swing.core)
