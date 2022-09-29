(ns humble-deck.controls
  (:require
    [humble-deck.resources :as resources]
    [humble-deck.slides :as slides]
    [humble-deck.state :as state]
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
  (when-some [cancel-timer (:controls-timer @state/*state)]
    (cancel-timer))
  (swap! state/*state assoc :controls-timer nil))

(defn hide-controls! []
  (when (= :present (:mode @state/*state))
    (swap! state/*state assoc :controls? false)
    (cancel-timer!)
    (app/doui
      (window/hide-mouse-cursor-until-moved @state/*window))))

(defn show-controls! []
  (cancel-timer!)
  (swap! state/*state assoc
    :controls-timer (core/schedule hide-controls! 2500)
    :controls?      true)
  (app/doui
    (window/hide-mouse-cursor-until-moved @state/*window false)))

(defn toggle-modes []
  (case (:mode @state/*state)
    :present  (swap! state/*state assoc
                :mode :overview
                :animation-end (core/now))
    :overview (swap! state/*state assoc
                :animation-start (core/now))))

(defn prev-slide []
  (swap! state/*state
    #(cond
       (> (:subslide %) 0)
       (update % :subslide dec)
                                      
       (> (:slide %) 0)
       (-> %
         (update :slide dec)
         (assoc :subslide (dec (count (nth slides/slides (dec (:slide %)))))))
                                      
       :else
       %)))

(defn next-slide []
  (swap! state/*state
    #(cond
       (< (:subslide %) (dec (count (nth slides/slides (:slide %)))))
       (update % :subslide inc)
                                    
       (< (:slide %) (dec (count slides/slides)))
       (-> %
         (update :slide inc)
         (assoc :subslide 0))
                                    
       :else
       %)))

(defn key-listener [child]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [prev-key? #{:up :left :page-up}
             next-key? #{:down :right :page-down :space}
             cmd?    (modifiers :mac-command)
             option? (modifiers :mac-option)
             window  @state/*window]
         (when
           (cond
             (and cmd? (prev-key? key))
             (swap! state/*state assoc
               :slide 0
               :subslide 0)

             (and cmd? (next-key? key))
             (swap! state/*state assoc
               :slide    (dec (count slides/slides))
               :subslide (dec (count (peek slides/slides))))

             (and option? (prev-key? key))
             (swap! state/*state
               #(assoc %
                  :slide    (max 0 (- (:slide %) 10))
                  :subslide 0))
             
             (and option? (next-key? key))
             (swap! state/*state
               #(assoc %
                  :slide    (min
                              (dec (count slides/slides))
                              (- (:slide %) 10))
                  :subslide 0))
             
             (prev-key? key)
             (prev-slide)

             (next-key? key)
             (next-slide)

             (= :t key)
             (toggle-modes)
             
             (= :f key)
             (let [full-screen? (window/full-screen? window)]
               (window/set-full-screen window (not full-screen?)))
             
             (and 
               (= :escape key)
               (window/full-screen? window))
             (window/set-full-screen window false))
           (hide-controls!))))}
    child))

(defmacro template-icon-button [icon & on-click]
  `(ui/width 40
     (ui/height 40
       (ui/button (fn [] ~@on-click)
         (ui/width 14
           (ui/height 14
             ~icon))))))

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
      (core/size (* scale thumb-w) (* scale thumb-h))))

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

(def controls
  (ui/mouse-listener
    {:on-move (fn [_] (show-controls!) false)
     :on-over (fn [_] (show-controls!))
     :on-out  (fn [_] (hide-controls!))}
    (ui/valign 1
      (ui/dynamic _ [controls? (:controls? @state/*state)]
        (if (not controls?)
          (ui/gap 0 0)
          (ui/mouse-listener
            {:on-move (fn [_] (cancel-timer!))}
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
                  (ui/row
                    (template-icon-button resources/icon-prev
                      (prev-slide)
                      #_(show-controls!))

                    (template-icon-button resources/icon-next
                      (next-slide)
                      #_(show-controls!))

                    (ui/gap 14 0)
                                      
                    [:stretch 1
                     (ui/dynamic _ [mode (:mode @state/*state)]
                       (if (= :present mode)
                         (ui/valign 0.5
                           (ui/slider
                             {:track-active   (->SliderTrackActive)
                              :track-inactive (->SliderTrackInactive)
                              :thumb          (->SliderThumb)}
                             state/*slider))
                         (ui/gap 0 0)))]
                    
                    (ui/gap 14 0)
                    
                    (ui/dynamic _ [mode (:mode @state/*state)]
                      (template-icon-button
                        (case mode
                          :overview resources/icon-present
                          :present  resources/icon-overview)
                        (toggle-modes)))
                    
                    (ui/dynamic ctx [window       (:window ctx)
                                     full-screen? (window/full-screen? window)]
                      (template-icon-button 
                        (if full-screen?
                          resources/icon-windowed
                          resources/icon-full-screen)
                        (window/set-full-screen window (not full-screen?))))))))))))))

(add-watch state/*state ::update-slider
  (fn [_ _ old new]
    (when (not= (:slide new) (:value @state/*slider))
      (swap! state/*slider assoc :value (:slide new)))))

(add-watch state/*slider ::rewind
  (fn [_ _ old new]
    (when (not= (:value new) (:slide @state/*state))
      (swap! state/*state assoc
        :slide    (:value new)
        :subslide (dec (count (nth slides/slides (:value new))))))))
