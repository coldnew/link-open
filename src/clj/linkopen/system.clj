(ns linkopen.system
  (:gen-class)
  (:use [clojure.tools.logging :only [info]]
        [clojure.java.shell :only [sh]])
  (:require [clojure.java.io :as io]))

(defn file?
  "Test if url is file and it exist."
  [url]
  (.exists (io/as-file url)))

;; TODO: Not a good way to determine OS platform
(defn open-file
  "Open file with os default application."
  [url]
  (println "go")
  (let [os (System/getProperty "os.name")]
    (cond
     (= os "Linux") (sh "xdg-open" url)
     (= os "Mac OS X") (sh "open" url)
     ;; for the f**k Windows platform
     :else
     (sh "cmd.exe" "/c" "start" url))))
