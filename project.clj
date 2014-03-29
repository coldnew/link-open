(defproject linkopen "0.1.0"
  :description "Open link or file directly."
  :url "http://github.com/coldnew/link-open"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [compojure "1.1.6" ]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-devel "1.1.8"]
                 [ring-cors "0.1.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [http-kit "2.1.18"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.4"]]

  :plugins [[lein-autoreload "0.1.0"]
            [lein-haml-sass "0.2.7-SNAPSHOT"]]

  ;; cljs
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/linkopen.js"
                           :optimizations :advanced
                           :pretty-print false}}]}

  ;; scss
  :scss {:src "src/scss"
         :output-directory "resources/public/css"
         :output-extension "css"}

  :omit-source true
  :profiles { :dev {:dependencies [[org.clojure/clojurescript "0.0-2173"]
                                   [enfocus "2.0.2"]] }}
  :aot :all
  :main linkopen.core)
