(ns humble-deck.controls
  (:require
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window]
    [humble-deck.core :refer :all])
  (:import
    [io.github.humbleui.skija FilterTileMode ImageFilter]))

(set! *warn-on-reflection* true)

(defn safe-add [x y from to]
  (-> (+ x y)
    (min (dec to))
    (max from)))

(defn key-listener [*state slides child]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [cmd? (modifiers :mac-command)]
         (cond
           (and cmd? (= :left key))
           (swap! *state update :current safe-add -1000000 0 (count slides))

           (and cmd? (= :right key))
           (swap! *state update :current safe-add 1000000 0 (count slides))

           (#{:up :left :page-up} key)
           (swap! *state update :current safe-add -1 0 (count slides))

           (#{:down :right :page-down :space} key)
           (swap! *state update :current safe-add 1 0 (count slides))
           
           
           (= :escape key)
           (case (:mode @*state)
             :present  (swap! *state assoc
                         :mode :overview
                         :end  (core/now))
             :overview (swap! *state assoc
                         :start (core/now))))))}
    child))

(defonce *controls-visible?
  (atom false))

(defonce *controls-timer
  (atom nil))

(defn hide-controls! []
  (reset! *controls-visible? false)
  (when-some [cancel-timer @*controls-timer]
    (cancel-timer))
  (reset! *controls-timer nil))

(defn show-controls! []
  (when-some [cancel-timer @*controls-timer]
    (cancel-timer))
  (reset! *controls-timer (core/schedule hide-controls! 5000))
  (not= @*controls-visible? (reset! *controls-visible? true)))

(defn template-icon [path]
  (ui/width 14
    (ui/height 14
      (ui/svg (str "resources/" path)))))

(def icon-prev
  (template-icon "prev.svg"))

(def icon-next
  (template-icon "next.svg"))

(def icon-overview
  (template-icon "overview.svg"))

(def icon-full-screen
  (template-icon "fullscreen.svg"))

(def icon-windowed
  (template-icon "windowed.svg"))

(defmacro template-icon-button [icon & on-click]
  `(ui/width 40
     (ui/height 40
       (ui/button (fn [] ~@on-click)
         ~icon))))

(defonce *slider
  (atom {:value 1
         :min   1
         :max   111}))

(add-watch *slider ::rewind
  (fn [_ _ old new]
    (when (not= (:value old) (:value new))
      (swap! *state assoc :current (dec (:value new))))))

(defn controls [*state slides]
  (ui/mouse-listener
    {:on-move (fn [_] (show-controls!))
     :on-over (fn [_] (show-controls!))
     :on-out  (fn [_] (hide-controls!))}
    (ui/valign 1
      (ui/dynamic _ [controls-visible? @*controls-visible?]
        (if (not controls-visible?)
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
                  (template-icon-button icon-prev
                    (swap! *state update :current safe-add -1 0 (count slides))
                    (show-controls!))

                  (template-icon-button icon-next
                    (swap! *state update :current safe-add 1 0 (count slides))
                    (show-controls!))

                  (ui/padding 14 0
                    (ui/valign 0.5
                      (ui/max-width
                        [(ui/label "888 / 888")]
                        (ui/halign 0
                          (ui/dynamic _ [current (:current @*state)]
                            (ui/label (format "%d / %d" (inc current) (count slides))))))))
                  
                  [:stretch 1
                   (ui/slider *slider)]
                      
                  (template-icon-button icon-overview
                    (swap! *state assoc
                      :mode :overview
                      :end  (core/now)))
                  
                  (ui/dynamic ctx [window       (:window ctx)
                                   full-screen? (window/full-screen? window)]
                    (if full-screen?
                      (template-icon-button icon-windowed
                        (window/set-full-screen window false))
                      (template-icon-button icon-full-screen
                        (window/set-full-screen window true)))))))))))))
 