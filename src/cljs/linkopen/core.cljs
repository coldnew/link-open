(ns linkopen.core
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects])
  (:require-macros [enfocus.macros :as em]))

(def eventbus
  (let [hostname js/window.location.hostname
        port js/window.location.port]
    (js/WebSocket. (str "ws://" hostname ":" port "/eventbus"))))

(defn by-id [id]
  (.getElementById js/document id))

(defn url?
  "Check if arg is a valid http url."
  [url]
  (let [p "(http|ftp|https)://[\\w-]+(\\.[\\w-]+)+([\\w.,@?^=%&amp;:/~+#-]*[\\w@?^=%&amp;/~+#-])?"
        ret (.test (js/RegExp. p "g") url)]
    ret))

(defn json-generate
  "Returns a newline-terminate JSON string from the given ClojureScript data."
  [data]
  (str (.stringify js/JSON (clj->js data)) "\n"))

(defn append-history
  [his]
  (ef/at "#history tbody > tr:first-child" (ef/before (ef/html [:tr [:td his]]))))

(defn send-event
  "Convert clojure map to json string and send to eventbus."
  [data]
  (let [json-data (str (.stringify js/JSON (clj->js data)))]
    (.send eventbus json-data)
    (.log js/console "send event : " json-data)))

(defn input-checker [evt]
  (let [val (.-value (by-id "qrInput"))]
    (when (= 13 (.-keyCode evt)) ;; 13 is Enter
      (append-history val)
      ;; send text to server, and make server to control event
      (send-event {:url val :link? (url? val)})
      ;; clean text input
      (set! (.-value (by-id "qrInput")) ""))))

(em/defaction setup-event-handler []
  "#qrInput" (events/listen :keydown input-checker))

(defn setup-focus []
  (.focus (by-id "qrInput")))

(defn start []
  ;; Listen to events
  (setup-event-handler)
  (setup-focus))

(set! (.-onload js/window) start)
