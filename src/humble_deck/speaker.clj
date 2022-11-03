(ns humble-deck.speaker
  (:require
    [humble-deck.common :as common]
    [humble-deck.controls :as controls]
    [humble-deck.state :as state]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [java.time LocalTime]
    [java.time.format DateTimeFormatter]))

(def speaker-app
  (controls/key-listener
    (ui/event-listener
      {:window-close-request
       (fn [_]
         (reset! state/*speaker-window nil))
       :key
       (fn [{:keys [pressed? modifiers key]}]
         (when (and 
                 pressed?
                 (modifiers :mac-command)
                 (= :w key))
           (common/speaker-close!)))}
      (common/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
        (ui/dynamic ctx [unit      (:unit ctx)
                         font-body (:font-body ctx)]
          (ui/with-context {:font-ui font-body}
            (ui/column
              [:stretch 1
               (ui/rect (paint/fill 0x80CC3333)
                 (ui/valign 0.5
                   (ui/row
                     [:stretch 1
                      (ui/halign 0.5
                        (ui/dynamic _ [now (.format (LocalTime/now) (DateTimeFormatter/ofPattern "HH:mm:ss"))]
                          (ui/label {:features ["tnum"]} now)))]
                     (ui/gap 10 0)
                     [:stretch 1
                      (ui/halign 0.5
                        (ui/label {:features ["tnum"]} "04:00"))])))]
              (ui/rect (paint/fill 0x8033CC33)
                (ui/dynamic _ [ratio (let [r (window/content-rect @state/*window)]
                                       (/ (:width r) (:height r)))]
                  (ui/height #(-> (:width %) (* 5/9) (/ ratio))
                    (ui/row
                      [:stretch 5
                       (ui/rect (paint/fill 0xFFFFFFFF)
                         (common/with-context
                           (ui/dynamic _ [{:keys [slide subslide]} @state/*state]
                             (-> @state/*slides (nth slide) (nth subslide) common/maybe-deref))))]
                      (ui/gap 10 0)
                      [:stretch 4
                       (ui/valign 0.5
                         (ui/dynamic _ [{:keys [slide subslide]} (common/slide-next @state/*state)]
                           (if (and slide subslide)
                             (ui/rect (paint/fill 0xFFFFFFFF)
                               (ui/height #(-> (:width %) (/ ratio))
                                 (common/with-context
                                   (-> @state/*slides (nth slide) (nth subslide) common/maybe-deref))))
                             (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
                               (ui/center
                                 (ui/label "Last slide"))))))]))))
              [:stretch 1
               (ui/valign 1
                 controls/controls-impl)])))))))
