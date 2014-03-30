(ns linkopen.browser
  (:import (javafx.scene Scene)
           (javafx.scene.layout BorderPane AnchorPane)
           (javafx.stage Stage)
           (javafx.scene.web WebView)
           (javafx.application Platform)
           (javafx.beans.value ChangeListener ObservableValue)
           (javafx.concurrent Worker$State)
           (javafx.scene.layout Region)
           (javafx.event ActionEvent EventHandler))
  (:require [linkopen.javafx :as jfx]))

;;;
;; We define three webview page as our application ui.
;;
;; ctrl-page: The control page locate at left of window.
;; info-page: Render url according text input in ctrl-page.
;; state-page: Show web-state info while info-page load other url and not finish.
;;
(defonce ctrl-page (atom nil))
(defonce info-page (atom nil))
(defonce state-page (atom nil))

(defonce host (atom nil))

(defonce STATE_WELCOME 0x1)
(defonce STATE_LOADING 0x2)
(defonce STATE_404 0x3)

(declare set-state-page)

(defn set-focus-to-ctrl-page []
  (jfx/run-later (.requestFocus @ctrl-page)))

(defn set-url [w url]
  (jfx/run-later (.load (.getEngine w) url))
  ;; Make focus back to control page
  (set-focus-to-ctrl-page))

(defn set-info-url [url]
  (set-state-page STATE_LOADING)
  (jfx/run-later (.setVisible @info-page false))
  (jfx/run-later (.setVisible @state-page true))
  (set-url @info-page url))

(defn set-state-url [url]
  (jfx/run-later (.setVisible @info-page false))
  (jfx/run-later (.setVisible @state-page true))
  (set-url @state-page url))

(defn set-state-page [x]
  (set-state-url
   (str @host
        (condp = x
          STATE_WELCOME "/welcome"
          STATE_LOADING "/loading"
          STATE_404  "/404"
          "/loading"))))

(defn fill-anchor-pane
  "Make node can fill-in AnchiPnae."
  [x]
  (AnchorPane/setTopAnchor x 0.0)
  (AnchorPane/setBottomAnchor x 0.0)
  (AnchorPane/setLeftAnchor x 0.0)
  (AnchorPane/setRightAnchor x 0.0))

(defn create [stage port]
  (let [root (BorderPane.)
        pane (AnchorPane.)
        web-control (WebView.)
        web-state (WebView.)
        web-info (WebView.)
        state-prop (.stateProperty (.getLoadWorker (.getEngine web-info)))
        url (str "http://127.0.0.1:" port)]

    (.load (.getEngine web-control) url)
    (.load (.getEngine web-info) (str url "/welcome"))
    (.load (.getEngine web-state) (str url "/loading"))

    (reset! host url)
    (reset! ctrl-page web-control)
    (reset! info-page web-info)
    (reset! state-page web-state)

    (.setPrefWidth web-control 235)

    (.addListener state-prop
                  (proxy [ChangeListener] []
                    (changed [^ObservableValue ov
                              ^Worker$State old-state
                              ^Worker$State new-state]
                      (when (= new-state Worker$State/SUCCEEDED)
                        (println (str "URL '" url "' load completed!"))
                        (.setVisible web-info true)
                        (set-focus-to-ctrl-page))
                      (when (= new-state Worker$State/FAILED)
                        (println (str "URL '" url "' load failed!"))
                        (set-state-page STATE_404)))))

    (.add (.getChildren pane) web-state)
    (.add (.getChildren pane) web-info )

    (fill-anchor-pane web-state)
    (fill-anchor-pane web-info)

    (doto root
      (.setLeft web-control)
      (.setCenter pane)
      (.setPrefSize 800 600))

    (.setVisible web-info true)
    (.requestFocus web-control)

    (doto stage
      (.setScene (Scene. root))
      (.setTitle "Link Open")
      (.show))))
