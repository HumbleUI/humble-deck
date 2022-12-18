(ns humble-deck.main
  (:require
    [humble-deck.common :as common]
    [humble-deck.controls :as controls]
    [humble-deck.overview :as overview]
    [humble-deck.slides :as slides]
    [humble-deck.speaker :as speaker]
    [humble-deck.state :as state]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.ui :as ui])
  (:import
    [io.github.humbleui.skija ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija LayerGLSkija]))

(def app
  (common/with-context
    (controls/key-listener
      (ui/stack
        (ui/dynamic _ [mode (:mode @state/*state)]
          (case mode
            :overview overview/overview
            :present  common/slide))
        controls/controls))))

(reset! state/*app app)

(reset! state/*slides slides/slides)

(reset! state/*speaker-app speaker/speaker-app)

(add-watch state/*state ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (common/redraw))))

(defn -main [& _args]
  (ui/start-app!
   (deliver state/*window
            (ui/window
             {:title    "Humble Deck"
              :mac-icon "resources/icon.icns"
              :bg-color 0xFFFFFFFF}
             state/*app)))
  ;; NOTE:
  ;; Should assess whether we can use Metal or GL here.
  ;; Might be having platform compatibility issues.
  (set! (.-_colorSpace ^LayerGLSkija (.getLayer ^Window @state/*window)) (ColorSpace/getDisplayP3))
                                        ; (reset! debug/*enabled? true)
  (common/redraw))
