(ns linkopen.javafx
  (:import (javafx.application Platform)))

(defmacro run-later
  "Runs `body` on the JavaFX Application Thread and blocks until execution has
  finished and returns its result."
  [& body]
  `(let [f# (fn [] ~@body)
         p# (promise)]
     (if (Platform/isFxApplicationThread)
       (f#)
       (do
         (Platform/runLater #(deliver p# (f#)))
         @p#))))
