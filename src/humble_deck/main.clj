(ns humble-deck.main
  (:require
    [humble-deck.controls :as controls]
    [humble-deck.overview :as overview]
    [humble-deck.resources :as resources]
    [humble-deck.slides :as slides]
    [humble-deck.speaker :as speaker]
    [humble-deck.state :as state]
    [humble-deck.templates :as templates]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui])
  (:import
    [io.github.humbleui.skija Color ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]))

(defn with-context [opts child]
  (ui/with-bounds ::bounds
    (ui/dynamic ctx [scale      (:scale ctx)
                     cap-height (-> ctx ::bounds :height (* scale) (quot 30))]
      (let [font-body    (font/make-with-cap-height resources/typeface-regular cap-height)
            font-h1      (font/make-with-cap-height resources/typeface-bold    cap-height)
            font-code    (font/make-with-cap-height resources/typeface-code    cap-height)]
        (ui/default-theme {:face-ui resources/typeface-regular}
          (ui/with-context
            (merge
              {:face-ui   resources/typeface-regular
               :font-ui   font-body
               :font-body font-body
               :font-h1   font-h1
               :font-code font-code
               :leading   (quot cap-height 2)
               :fill-text (paint/fill 0xFF212B37)
               :unit      (quot cap-height 10)}
              opts)
            child))))))

(def app  
  (with-context {}
    (controls/key-listener
      (ui/stack
        (ui/dynamic _ [mode (:mode @state/*state)]
          (case mode
            :overview overview/overview
            :present  slides/slide))
        controls/controls))))

(reset! state/*app app)

(def speaker-app
  (with-context {:fill-text (paint/fill 0xFFFFFFFF)}
    (controls/key-listener
      speaker/app)))

(reset! state/*speaker-app speaker-app)

(defn -main [& args]
  (ui/start-app!
    (deliver state/*window
      (ui/window
        {:title    "Humble Deck"
         :mac-icon "resources/icon.icns"
         :bg-color 0xFFFFFFFF}
        state/*app)))
  (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @state/*window)) (ColorSpace/getDisplayP3))
  ; (reset! debug/*enabled? true)
  (state/redraw))
