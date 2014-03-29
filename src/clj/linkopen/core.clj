(ns linkopen.core
  (:import (javafx.application Application))
  (:require [linkopen.server :as server]
            [linkopen.browser :as browser]))

(gen-class
 :name linkopen.core
 :extends javafx.application.Application)

(defn -main [& args]
  (Application/launch linkopen.core args))

(defn generate-port
  "Generate usable port for localhost."
  []
  )

(defn -start [this stage]
  ;; start server to control pages
  (server/start-server)
  ;; Show mainwindow
  (browser/create stage 8080))

(defn -stop [app]
  (println "Exiting applications!")
  (server/stop-server))
