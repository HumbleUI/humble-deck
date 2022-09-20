(ns humble-deck.core
  (:require
    [io.github.humbleui.typeface :as typeface]
    [io.github.humbleui.window :as window]))

(set! *warn-on-reflection* true)

(def ratio
  #_(/ 16 9)
  (/ 3024 (- 1964 (* 2 37))))

(defonce *state
  (atom {:current 0
         :mode    :present}))

(def typeface-regular
  (typeface/make-from-path "resources/CaseMicro-Regular.otf"))

(def typeface-bold
  (typeface/make-from-path "resources/CaseMicro-Bold.otf"))

(defonce *window
  (atom nil))

(defn redraw []
  (some-> @*window window/request-frame))
