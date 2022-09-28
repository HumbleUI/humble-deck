(ns ^{:clojure.tools.namespace.repl/load false}
  humble-deck.state
  (:require
    [io.github.humbleui.window :as window]))

(def *app
  (atom nil))

(def *window
  (promise))

(defn redraw []
  (window/request-frame @*window))

(def *state
  (atom {:current         0
         :mode            :present
         :animation-start nil
         :animation-end   nil
         :controls?       true
         :controls-timer  nil}))

(add-watch *state ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(defonce *slider
  (atom {:value 1
         :min   1
         :max   111}))

(add-watch *state ::update-slider
  (fn [_ _ old new]
    (when (not= (:current old) (:current new))
      (swap! *slider assoc :value (inc (:current new))))))

(add-watch *slider ::rewind
  (fn [_ _ old new]
    (when (not= (:value old) (:value new))
      (swap! *state assoc :current (dec (:value new))))))

