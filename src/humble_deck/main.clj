(ns humble-deck.main
  (:require
    [clojure.java.io :as io]
    [humble-deck.controls :as controls]
    [humble-deck.scaler :as scaler]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.typeface :as typeface]
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

(def typeface-regular
  (typeface/make-from-path "resources/CaseMicro-Regular.otf"))

(def typeface-bold
  (typeface/make-from-path "resources/CaseMicro-Bold.otf"))

(def *slide0
  (delay
    (scaler/scaler 1
      (ui/svg (io/file "resources/slide 0.svg")))))

(def *slide1
  (delay
    (ui/dynamic ctx [{:keys [font-body font-h1 leading]} ctx]
      (ui/column
        (ui/with-context {:font-ui font-h1}
          (ui/label "Why Clojure?"))
        (ui/gap 0 leading)
        (ui/label "• REPL")
        (ui/gap 0 leading)
        (ui/label "• Utilize computer")))))

(def *slide2
  (delay
    (ui/dynamic ctx [{:keys [leading]} ctx]
      (ui/column
        (ui/halign 0.5
          (ui/label "Thank you for"))
        (ui/gap 0 leading)
        (ui/halign 0.5
          (ui/label "your attention!"))))))

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
  (ui/with-bounds ::bounds
    (ui/dynamic ctx [cap-height (-> (::bounds ctx) :height (quot 10))]
      (let [font-body (font/make-with-cap-height typeface-regular cap-height)
            font-h1 (font/make-with-cap-height typeface-bold cap-height)]
        (ui/default-theme
          {:face-ui typeface-regular}
          (ui/with-context
            {:font-body font-body
             :font-h1   font-h1
             :leading   (quot cap-height 2)}
            (ui/mouse-listener
              {:on-move (fn [_] (controls/show-controls!))
               :on-over (fn [_] (controls/show-controls!))
               :on-out  (fn [_] (controls/hide-controls!))}
              (ui/stack
                (ui/halign 0.5
                  (ui/valign 0.5
                    (ui/rect (paint/fill 0xFFFFFFFF)
                      (ui/dynamic ctx [{:keys [font-body]} ctx
                                       current @*current]
                        (ui/with-context
                          {:font-ui font-body}
                          @(nth slides current))))))
                (ui/halign 0.5
                  (ui/valign 1
                    (ui/padding 0 0 0 20
                      (controls/controls *current slides))))))))))))

(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title    "Humble 🐝 Deck"
       :mac-icon "resources/icon.icns"
       :bg-color 0xFFFFFFFF}
      #'app)))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))