(ns humble-deck.main
  (:require
    [humble-deck.controls :as controls]
    [humble-deck.overview :as overview]
    [humble-deck.resources :as resources]
    [humble-deck.slides :as slides]
    [humble-deck.state :as state]
    [humble-deck.templates :as templates]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui])
  (:import
    [io.github.humbleui.skija Color ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]))

(def app
  (ui/default-theme {:face-ui resources/typeface-regular}
    (ui/with-context {:fill-text (paint/fill 0xFF212B37)}
      (controls/key-listener
        (ui/stack
          (ui/dynamic _ [mode (:mode @state/*state)]
            (case mode
              :overview overview/overview
              :present  slides/slide))
          controls/controls)))))

(reset! state/*app app)

(defn -main [& args]
  (ui/start-app!
    (deliver state/*window
      (ui/window
        {:title    "Humble Deck"
         :mac-icon "resources/icon.icns"
         :bg-color 0xFFFFFFFF}
        state/*app)))
  (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @state/*window)) (ColorSpace/getDisplayP3))
  (state/redraw))
