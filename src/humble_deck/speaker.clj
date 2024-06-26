(ns humble-deck.speaker
  (:refer-clojure :exclude [time])
  (:require
    [humble-deck.common :as common]
    [humble-deck.controls :as controls]
    [humble-deck.resources :as resources]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.skija FilterTileMode ImageFilter]))

(defn talk-reset! []
  (swap! common/*state assoc
    :speaker-time    1000000
    :speaker-start   nil))

(defn talk-resume! []
  (swap! common/*state assoc
    :speaker-start (- (core/now) 1000)))

(defn time-passed [state]
  (let [{:keys [speaker-time speaker-start]} state]
    (+ speaker-time (if speaker-start (- (core/now) speaker-start) 0))))

(defn talk-pause! []
  (swap! common/*state
    (fn [state]
      (assoc state
        :speaker-time  (time-passed state)
        :speaker-start nil))))

(def time
  (ui/center
    (ui/dynamic ctx [unit (:unit ctx)]
      (ui/row
        (ui/valign 0.5
          (ui/width (* unit 10)
            (ui/height (* unit 10)
              resources/icon-clock)))
        (ui/gap (* unit 2) 0)
        (ui/valign 0.5
          (ui/dynamic ctx [font-body (:font-body ctx)
                           now (common/time-format-HH-mm (core/now))]
            (ui/with-context {:font-ui font-body}
              (ui/label {:features ["tnum"]} now))))))))

(def slides
  (ui/padding 10 0
    (ui/dynamic _ [ratio (let [r (window/content-rect @common/*window)]
                           (/ (:width r) (:height r)))]
      (ui/height #(-> (:width %) (* 5/9) (/ ratio))
        (ui/row
          [:stretch 5
           (ui/rect (paint/fill 0xFFFFFFFF)
             (common/with-context
               (ui/dynamic _ [{:keys [slide subslide]} @common/*state]
                 (-> @common/*slides (nth slide) (nth subslide) common/maybe-deref))))]
          (ui/gap 10 0)
          [:stretch 4
           (ui/valign 0.5
             (ui/dynamic _ [{:keys [slide subslide]} (common/slide-next @common/*state)]
               (if (and slide subslide)
                 (ui/rect (paint/fill 0xFFFFFFFF)
                   (ui/height #(-> (:width %) (/ ratio))
                     (common/with-context
                       (-> @common/*slides (nth slide) (nth subslide) common/maybe-deref))))
                 (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
                   (ui/center
                     (ui/label "Last slide"))))))])))))

(defn progress [percent]
  (ui/clip-rrect 1.5
    (ui/row
      [:stretch percent
       (ui/rect (paint/fill 0xE0FFFFFF)
         (ui/width #(:width %)
           (ui/gap 0 3)))]
      (ui/gap 3 0)
      [:stretch (- 100 percent)
       (ui/rect (paint/fill 0x50FFFFFF)
         (ui/width #(:width %)
           (ui/gap 0 3)))])))

(def progress-controls
  (ui/with-context
    {:fill-text                 (paint/fill 0xE0FFFFFF)
     :hui.button/bg-active      (paint/fill 0x80000000)
     :hui.button/bg-hovered     (paint/fill 0x40000000)
     :hui.button/bg             (paint/fill 0x00000000)
     :hui.button/padding-left   0
     :hui.button/padding-top    0
     :hui.button/padding-right  0
     :hui.button/padding-bottom 0
     :hui.button/border-radius  0}
    (ui/backdrop (ImageFilter/makeBlur 70 70 FilterTileMode/CLAMP)
      (ui/rect (paint/fill 0x50000000)
        (ui/dynamic _ [active? (some? (:speaker-start @common/*state))]
          (ui/row
            (common/template-icon-button
              (if active?
                resources/icon-talk-pause
                resources/icon-talk-resume)
              (if active?
                (talk-pause!)
                (talk-resume!)))
            (ui/width 40
              (ui/center
                (ui/dynamic _ [passed (-> (time-passed @common/*state)
                                        (common/duration-format-mm-ss))]
                  (ui/label {:features ["tnum"]} passed))))
            (ui/gap 14 0)
            [:stretch 1
             (ui/valign 0.5
               (ui/dynamic _ [percent (-> (time-passed @common/*state)
                                        (/ @common/*talk-duration)
                                        (* 100)
                                        (long)
                                        (min 100))]
                 (progress percent)))]
            (ui/gap 14 0)
            (ui/width 40
              (ui/center
                (ui/dynamic _ [left (-> @common/*talk-duration
                                      (- (time-passed @common/*state))
                                      (common/duration-format-mm-ss))]
                  (ui/label {:features ["tnum"]} left))))
            (common/template-icon-button resources/icon-talk-reset
              (talk-reset!))
            (ui/gap 40 0)))))))

(def speaker-app
  (controls/key-listener
    (ui/event-listener :window-close-request
      (fn [_ _]
        (reset! common/*speaker-window nil))
      (ui/event-listener :key
        (fn [{:keys [pressed? modifiers key]} _]
          (when (and 
                  pressed?
                  (modifiers :mac-command)
                  (= :w key))
            (common/speaker-close!)))
        (common/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
          (ui/column
            [:stretch 1 time]
            slides
            [:stretch 1
             (ui/valign 1
               (ui/column
                 progress-controls
                 controls/controls-impl))]))))))

(reset! common/*speaker-app speaker-app)
