(ns humble-deck.main
  (:require
    [humble-deck.common :as common]
    [humble-deck.controls :as controls]
    [humble-deck.overview :as overview]
    [humble-deck.speaker :as speaker]
    [slides :as slides]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.skija ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]))

(def app
  (common/with-context
    (controls/key-listener
      (ui/stack
        (ui/dynamic _ [{:keys [mode]} @common/*state]
          (case mode
            :overview overview/overview
            :present  common/slide))
        controls/controls))))

(defn -main [& args]
  (ui/start-app!
    (let [w (ui/window
              {:title    "Humble Deck"
               :mac-icon "resources/icon.icns"
               :bg-color 0xFFFFFFFF
               :width    711
               :height   400
               :x        :right
               :y        :top}
              #'app)]
      (window/set-z-order w :floating)
      (when (= :macos app/platform)
        (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window w)) (ColorSpace/getDisplayP3)))
      (reset! common/*window w))))
