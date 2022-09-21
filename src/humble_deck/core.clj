(ns humble-deck.core
  (:require
    [io.github.humbleui.typeface :as typeface]
    [io.github.humbleui.window :as window]))

(set! *warn-on-reflection* true)

(defonce *state
  (atom {:current 0
         :mode    :present}))

(def typeface-regular
  (typeface/make-from-path "resources/CaseMicro-Regular.otf"))

(def typeface-bold
  (typeface/make-from-path "resources/CaseMicro-Bold.otf"))

(def typeface-code
  (typeface/make-from-path "resources/MartianMono-StdRg.otf"))

(defonce *window
  (atom nil))

(defn redraw []
  (some-> @*window window/request-frame))
