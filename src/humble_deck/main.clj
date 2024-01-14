(ns humble-deck.main
  (:require
    [humble-deck.common :as common]
    [humble-deck.controls :as controls]
    [humble-deck.overview :as overview]
    [humble-deck.speaker :as speaker]
    [humble-deck.state :as state]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.ui :as ui])
  (:import
    [io.github.humbleui.skija ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]))

(def app
  (common/with-context
    (controls/key-listener
      (ui/stack
        (ui/dynamic _ [{:keys [mode]} @state/*state]
          (case mode
            :overview overview/overview
            :present  common/slide))
        controls/controls))))

(defn -main [& args]
  (let [args (apply hash-map args)]
    (when-some [deck (get args "--deck")]
      (alter-var-root #'state/deck (constantly deck))))
  
  (load-file (str "decks/" state/deck "/slides.clj"))

  (reset! state/*app app)

  (reset! state/*slides @(resolve 'slides/slides))

  (reset! state/*speaker-app speaker/speaker-app)

  (add-watch state/*state ::redraw
    (fn [_ _ old new]
      (when (not= old new)
        (common/redraw))))

  (ui/start-app!
    (deliver state/*window
      (ui/window
        {:title    "Humble Deck"
         :mac-icon "resources/icon.icns"
         :bg-color 0xFFFFFFFF
         :width    711
         :height   400
         :x        :center
         :y        :center}
        state/*app)))
  (when (= :macos app/platform)
    (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @state/*window)) (ColorSpace/getDisplayP3)))
  ; (reset! debug/*enabled? true)
  (common/redraw))
