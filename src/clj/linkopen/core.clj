(ns linkopen.core
  (:import (javafx.application Application))
  (:require [linkopen.server :as server]
            [linkopen.browser :as browser]))

(gen-class
 :name linkopen.core
 :extends javafx.application.Application)

(defn -main [& args]
  (Application/launch linkopen.core args))

(def port
  (let [skt (java.net.ServerSocket. 0)
        port (.getLocalPort skt)]
    (.close skt) port))

(defn -start [this stage]
  ;; start server to control pages
  (server/start-server port)
  ;; Show mainwindow
  (browser/create stage port))

(defn -stop [app]
  (println "Exiting applications!")
  (server/stop-server))
