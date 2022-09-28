(ns humble-deck.controls
  (:require
    [humble-deck.resources :as resources]
    [humble-deck.slides :as slides]
    [humble-deck.state :as state]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.skija FilterTileMode ImageFilter]))

(defn safe-add [x y from to]
  (-> (+ x y)
    (min (dec to))
    (max from)))

(defn toggle-modes []
  (case (:mode @state/*state)
    :present  (swap! state/*state assoc
                :mode :overview
                :animation-end (core/now))
    :overview (swap! state/*state assoc
                :animation-start (core/now))))

(defn key-listener [child]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [cmd?   (modifiers :mac-command)
             window @state/*window]
         (cond
           (and cmd? (= :left key))
           (swap! state/*state update :current safe-add -1000000 0 (count slides/slides))

           (and cmd? (= :right key))
           (swap! state/*state update :current safe-add 1000000 0 (count slides/slides))

           (#{:up :left :page-up} key)
           (swap! state/*state update :current safe-add -1 0 (count slides/slides))

           (#{:down :right :page-down :space} key)
           (swap! state/*state update :current safe-add 1 0 (count slides/slides))
           
           (= :t key)
           (toggle-modes)
           
           (= :f key)
           (let [full-screen? (window/full-screen? window)]
             (window/set-full-screen window (not full-screen?)))
           
           (and 
             (= :escape key)
             (window/full-screen? window))
           (window/set-full-screen window false))))}
    child))

(defn hide-controls! []
  (swap! state/*state assoc :controls? false)
  (when-some [cancel-timer (:controls-timer @state/*state)]
    (cancel-timer))
  (swap! state/*state assoc :controls-timer nil))

(defn show-controls! []
  (when-some [cancel-timer (:controls-timer @state/*state)]
    (cancel-timer))
  (swap! state/*state assoc
    :controls-timer (core/schedule hide-controls! 5000)
    :controls?      true))

(defmacro template-icon-button [icon & on-click]
  `(ui/width 40
     (ui/height 40
       (ui/button (fn [] ~@on-click)
         (ui/width 14
           (ui/height 14
             ~icon))))))

(def controls
  (ui/mouse-listener
    {:on-move (fn [_] (show-controls!))
     :on-over (fn [_] (show-controls!))
     :on-out  (fn [_] (hide-controls!))}
    (ui/valign 1
      (ui/dynamic _ [controls? (:controls? @state/*state)]
        (if (not controls?)
          (ui/gap 0 0)
          (ui/with-context
            {:fill-text                 (paint/fill 0xCCFFFFFF)
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
                    (swap! state/*state update :current safe-add -1 0 (count slides/slides))
                    (show-controls!))

                  (template-icon-button resources/icon-next
                    (swap! state/*state update :current safe-add 1 0 (count slides/slides))
                    (show-controls!))

                  (ui/padding 14 0
                    (ui/valign 0.5
                      (ui/max-width
                        [(ui/label "888 / 888")]
                        (ui/halign 0
                          (ui/dynamic _ [current (:current @state/*state)]
                            (ui/label (format "%d / %d" (inc current) (count slides/slides))))))))
                  
                  [:stretch 1
                   (ui/slider state/*slider)]
                      
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
                      (window/set-full-screen window (not full-screen?)))))))))))))
