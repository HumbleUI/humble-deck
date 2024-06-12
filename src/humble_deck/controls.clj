(ns humble-deck.controls
  (:require
    [humble-deck.common :as common]
    [humble-deck.resources :as resources]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.skija FilterTileMode ImageFilter]))

(defn cancel-timer! []
  (when-some [cancel-timer (:controls-timer @common/*state)]
    (cancel-timer))
  (swap! common/*state assoc :controls-timer nil))

(defn hide-controls! []
  (when (= :present (:mode @common/*state))
    (swap! common/*state assoc :controls? false)
    (cancel-timer!)
    (app/doui
      (window/hide-mouse-cursor-until-moved @common/*window))))

(defn show-controls! []
  (cancel-timer!)
  (swap! common/*state assoc
    :controls-timer (core/schedule hide-controls! 2500)
    :controls?      true)
  (app/doui
    (window/hide-mouse-cursor-until-moved @common/*window false)))

(defn toggle-modes []
  (case (:mode @common/*state)
    :present  (swap! common/*state assoc
                :mode :overview
                :animation-end (core/now))
    :overview (swap! common/*state assoc
                :animation-start (core/now))))

(defn key-listener [child]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [prev-key? #{:up :left :page-up}
             next-key? #{:down :right :page-down :space}
             cmd?    (modifiers :mac-command)
             option? (modifiers :mac-option)
             window  @common/*window]
         (when
           (cond
             (and cmd? (prev-key? key))
             (swap! common/*state common/slide-first)

             (and cmd? (next-key? key))
             (swap! common/*state common/slide-last)

             (and option? (prev-key? key))
             (swap! common/*state common/slide-prev-10)
             
             (and option? (next-key? key))
             (swap! common/*state common/slide-next-10)
             
             (prev-key? key)
             (swap! common/*state #(or (common/slide-prev %) %))

             (next-key? key)
             (swap! common/*state #(or (common/slide-next %) %))

             (= :t key)
             (toggle-modes)
             
             (= :f key)
             (let [full-screen? (window/full-screen? window)]
               (window/set-full-screen window (not full-screen?)))
             
             (= :s key)
             (core/schedule #(app/doui (common/speaker-toggle!)) 0)
             
             (and 
               (= :escape key)
               (window/full-screen? window))
             (window/set-full-screen window false))
           (hide-controls!))))}
    child))

(def thumb-w
  3)

(def thumb-h
  20)

(def thumb-r
  1.5)

(def thumb-padding
  3)

(def track-h
  3)

(def track-r
  1.5)

(core/deftype+ SliderThumb []
  protocols/IComponent
  (-measure [_ ctx cs]
    (let [{:keys [scale]} ctx]
      (core/isize (* scale thumb-w) (* scale thumb-h))))

  (-draw [this ctx rect ^Canvas canvas]
    (let [{:keys [scale]} ctx
          rrect (core/rrect-xywh (:x rect) (:y rect) (:width rect) (:height rect) (* scale thumb-r))]
      (with-open [fill (paint/fill 0xE0FFFFFF)]
        (canvas/draw-rect canvas rrect fill))))
  
  (-event [this ctx event])

  (-iterate [this ctx cb]))

(core/deftype+ SliderTrackActive []
  protocols/IComponent
  (-measure [_ ctx cs]
    cs)

  (-draw [this ctx ^IRect rect ^Canvas canvas]
    (let [{:keys [scale]} ctx
          track-h      (* scale track-h)
          half-track-h (/ track-h 2)
          half-thumb-w (-> thumb-w (* scale) (/ 2))
          x            (- (:x rect) half-track-h)
          y            (+ (:y rect) (/ (:height rect) 2) (- half-track-h))
          w            (+ (:width rect) half-track-h (- half-thumb-w) (- (* thumb-padding scale)))
          track-r      (* scale track-r)
          rect         (core/rrect-xywh x y w track-h track-r 0 0 track-r)]
      (when (pos? w)
        (with-open [fill (paint/fill 0xE0FFFFFF)]
          (canvas/draw-rect canvas rect fill)))))
  
  (-event [this ctx event])

  (-iterate [this ctx cb]))

(core/deftype+ SliderTrackInactive []
  protocols/IComponent
  (-measure [_ ctx cs]
    cs)

  (-draw [this ctx ^IRect rect ^Canvas canvas]
    (let [{:keys [scale]}   ctx
          track-h      (* scale track-h)
          half-track-h (/ track-h 2)
          half-thumb-w (-> thumb-w (* scale) (/ 2))
          x            (+ (:x rect) half-thumb-w (* thumb-padding scale))
          y            (+ (:y rect) (/ (:height rect) 2) (- half-track-h))
          w            (+ (:width rect) half-track-h (- half-thumb-w) (- (* thumb-padding scale)))
          track-r      (* scale track-r)
          rect         (core/rrect-xywh x y w track-h 0 track-r track-r 0)]
      (when (pos? w)
        (with-open [fill (paint/fill 0x50FFFFFF)]
          (canvas/draw-rect canvas rect fill)))))
  
  (-event [this ctx event])

  (-iterate [this ctx cb]))

(def controls-impl
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
        (ui/dynamic _ [{:keys [mode]} @common/*state]
          (ui/row
            (common/template-icon-button resources/icon-prev
              (swap! common/*state #(or (common/slide-prev %) %)))

            (common/template-icon-button resources/icon-next
              (swap! common/*state #(or (common/slide-next %) %)))

            (ui/gap 14 0)
                                        
            [:stretch 1
             (ui/valign 0.5
               (ui/slider
                 {:track-active   (->SliderTrackActive)
                  :track-inactive (->SliderTrackInactive)
                  :thumb          (->SliderThumb)}
                 common/*slider))]
                      
            (ui/gap 14 0)
                      
            (ui/dynamic _ [mode (:mode @common/*state)]
              (common/template-icon-button
                (case mode
                  :overview resources/icon-present
                  :present  resources/icon-overview)
                (toggle-modes)))
                      
            (common/template-icon-button resources/icon-speaker
              (common/speaker-toggle!))
                      
            (ui/dynamic ctx [window       (:window ctx)
                             full-screen? (window/full-screen? window)]
              (common/template-icon-button 
                (if full-screen?
                  resources/icon-windowed
                  resources/icon-full-screen)
                (window/set-full-screen window (not full-screen?))))))))))

(def controls
  (ui/mouse-listener
    {:on-move (fn [_] (show-controls!) false)
     :on-over (fn [_] (show-controls!))
     :on-out  (fn [_] (hide-controls!))}
    (ui/valign 1
      (ui/dynamic _ [{:keys [controls?]} @common/*state]
        (if (not controls?)
          (ui/gap 0 0)
          (ui/mouse-listener
            {:on-move (fn [_] (cancel-timer!))}
            controls-impl))))))

(add-watch common/*state ::update-slider
  (fn [_ _ _ new]
    (when (not= (:slide new) (:value @common/*slider))
      (swap! common/*slider assoc :value (:slide new)))))

(add-watch common/*slider ::rewind
  (fn [_ _ _ new]
    (when (not= (:value new) (:slide @common/*state))
      (swap! common/*state assoc
        :slide    (:value new)
        :subslide (dec (count (nth @common/*slides (:value new))))))))
