(ns ^{:clojure.tools.namespace.repl/load false}
  humble-deck.state
  (:require
    [io.github.humbleui.window :as window]))

(def *app
  (atom nil))

(def *window
  (promise))

(def *speaker-window
  (atom nil))

(def *speaker-app
  (atom nil))

(defn redraw []
  (window/request-frame @*window)
  (some-> *speaker-window deref window/request-frame))

(def *state
  (atom
    {:slide           0
     :subslide        0
     :mode            :present
     :animation-start nil
     :animation-end   nil
     :controls?       true
     :controls-timer  nil}))

(add-watch *state ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(def *slider
  (atom
    {:value 0
     :min   0}))

(def *image-snapshot?
  (atom false))