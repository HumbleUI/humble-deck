(ns humble-deck.main
  (:require
    [clojure.java.io :as io]
    [humble-deck.controls :as controls]
    [humble-deck.scaler :as scaler]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.types IPoint IRect]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

(defonce *window
  (atom nil))

(defn redraw []
  (some-> @*window window/request-frame))

(def *slide0
  (delay
    (ui/image (io/file "resources/slide 0.png"))))

(def *slide1
  (delay
    (ui/padding 10
      (ui/label "Slide 1"))))

(def *slide2
  (delay
    (ui/padding 10
      (ui/column
        (ui/halign 0.5
          (ui/label "Thank you"))
        (ui/gap 0 10)
        (ui/halign 0.5
          (ui/label "for your attention!"))))))

(def slides
  [*slide0
   *slide1
   *slide2])

(defonce *current
  (atom 0))

(add-watch *current ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(add-watch controls/*controls-visible? ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(def app
  (ui/default-theme
    (ui/mouse-listener
      {:on-move (fn [_] (controls/show-controls!))
       :on-over (fn [_] (controls/show-controls!))
       :on-out  (fn [_] (controls/hide-controls!))}
      (ui/stack
        (ui/halign 0.5
          (ui/valign 0.5
            (scaler/scaler
              (ui/rect (paint/fill 0xFFFFFFFF)
                (ui/dynamic _ [current @*current]
                  @(nth slides current))))))
        (ui/halign 0.5
          (ui/valign 1
            (ui/padding 0 0 0 20
              (controls/controls *current slides))))))))

(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title    "Humble 🐝 Deck"
       :mac-icon "resources/icon.icns"
       :bg-color 0xFFEEEEEE}
      #'app)))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))