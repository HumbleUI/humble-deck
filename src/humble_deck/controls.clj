(ns humble-deck.controls
  (:require
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui])
  (:import
    [io.github.humbleui.skija FilterTileMode ImageFilter]))

(set! *warn-on-reflection* true)

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
  (reset! *controls-visible? true)
  (when-some [cancel-timer @*controls-timer]
    (cancel-timer))
  (reset! *controls-timer (core/schedule hide-controls! 5000)))

(defn safe-add [x y from to]
  (-> (+ x y)
    (min (dec to))
    (max from)))

(def icon-prev
  (ui/width 14
    (ui/height 14
      (ui/svg "resources/prev.svg"))))

(def icon-next
  (ui/width 14
    (ui/height 14
      (ui/svg "resources/next.svg"))))

(def icon-overview
  (ui/width 14
    (ui/height 14
      (ui/svg "resources/overview.svg"))))

(defn controls [*state slides]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [cmd? (modifiers :mac-command)]
         (cond
           (and cmd? (= :left key))
           (swap! *state update :current safe-add -1000000 0 (count slides))

           (and cmd? (= :right key))
           (swap! *state update :current safe-add 1000000 0 (count slides))

           (= :left key)
           (swap! *state update :current safe-add -1 0 (count slides))

           (#{:right :space} key)
           (swap! *state update :current safe-add 1 0 (count slides)))))}
    (ui/dynamic _ [controls-visible? @*controls-visible?]
      (if (not controls-visible?)
        (ui/gap 0 0)
        (ui/with-context
          {:fill-text                (paint/fill 0xCCFFFFFF)
           :hui.button/bg-active     (paint/fill 0x80000000)
           :hui.button/bg-hovered    (paint/fill 0x40000000)
           :hui.button/bg            (paint/fill 0x00000000)
           :hui.button/border-radius 0}
          (ui/clip-rrect 8
            (ui/backdrop (ImageFilter/makeBlur 10 10 FilterTileMode/CLAMP)
              (ui/rect
                (paint/fill 0x60000000)
                (ui/row
                  (ui/width 50
                    (ui/height 50
                      (ui/button #(do
                                    (swap! *state update :current safe-add -1 0 (count slides))
                                    (show-controls!))
                        icon-prev)))
                  
                  (ui/width 50
                    (ui/height 50
                      (ui/button #(do
                                    (swap! *state update :current safe-add 1 0 (count slides))
                                    (show-controls!))
                        icon-next)))
                                      
                  #_(ui/width 70
                    (ui/center
                      (ui/label (format "%d / %d" (inc current) (count slides)))))
                  
                  (ui/width 50
                    (ui/height 50
                      (ui/button #(swap! *state assoc
                                    :mode :overview
                                    :end  (core/now))
                        icon-overview)))
                  )))))))))
