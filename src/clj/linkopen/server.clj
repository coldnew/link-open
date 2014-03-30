(ns linkopen.server
  (:gen-class)
  (:use org.httpkit.server
        [clojure.tools.logging :only [info]]
        [compojure.core :only [defroutes GET POST]]
        [compojure.route :only [files resources not-found]]
        [compojure.handler :only [site]]
        [ring.middleware.cors :only [wrap-cors]]
        [ring.middleware.reload :only [wrap-reload]]
        [ring.middleware.stacktrace :only [wrap-stacktrace]]
        [clojure.java.shell :only [sh]]
        [hiccup.page :only [html5 include-js include-css]])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [ring.util.response :as response]
            [linkopen.browser :as browser]
            [linkopen.system :as sys]))

(defmacro get-version []
  (System/getProperty "linkopen.version"))

(defonce server (atom nil))
(defonce text (atom ""))

(defn handler [req]
  (with-channel req channel              ; get the channel
    ;; communicate with client using method defined above
    (on-close channel (fn [status]
                        (println "channel closed")))
    (if (websocket? channel)
      (println "WebSocket channel") (println "HTTP channel"))
    ;; data received from client
    (on-receive channel (fn [data]
                          (let [json-data (json/read-str data :key-fn keyword)
                                url (:url json-data)
                                link? (:link? json-data)
                                file? (sys/file? url)]
                            (println "Receive message: " (str data))
                            (cond
                             file? (sys/open-file url)
                             link? (browser/set-info-url url))
                            (reset! text url)))
                )))

(defn render-index []
  (html5
   [:head
    (include-js "/js/linkopen.js")
    (include-css "/css/control.css")]
   [:body
    [:h1 "Insert link here"]
    [:input {:id "qrInput" :type "text" :placeholder "Insert link here !!" :size 30}]
    [:p "history"]
    [:table {:id "history" :class "table" }
     [:thead [:tr [:th ""]]]
     [:tbody [:tr ]]]
    [:div {:id "version"} [:p "Version: " (get-version)]]]))

(defn render-info []
  (html5
   [:head ]
   [:body [:p @text] ]))

(defn render-loading []
  (html5
   [:head
    (include-css "/css/loading.css") ]
   [:body
    [:div {:class "center"}
     [:div {:class "circle"}]
     [:div {:class "circle1"}]]]))

(defn render-404 []
  (html5
   [:head
    (include-css "/css/404.css") ]
   [:body ]))

(defn render-welcome []
  (html5
   [:head
    (include-css "/css/welcome.css") ]
   [:body ]))

(defroutes app-routes
  (GET "/" [] (render-index))
  (GET "/welcome" [] (render-welcome))
  (GET "/info" [] (render-info))
  (GET "/loading" [] (render-loading))
  (GET "/eventbus" [] handler)
  (resources "/")
  (not-found (render-404)))

(def app
  (-> (site app-routes)
      (wrap-reload '(linkopen.server))
      (wrap-cors :access-control-allow-origin #".+")
      (wrap-stacktrace)))

(defn start-server
  "Start server on localhost. If port argument doesn't specify,
use env variable PORT or 8080 as default port."
  ([] (start-server (Integer/parseInt (or (System/getenv "PORT") "8080"))))
  ([port]
     (println (str "Project Version: " (get-version)))
     (reset! server (run-server app {:port port}))
     (info (str "server started. http://127.0.0.1:" port))))

(defn stop-server
  "Shudown the server, wait 100ms for existing requests to be finished."
  []
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)
    (info "server stopped.")))
