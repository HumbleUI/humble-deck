(ns humble-deck.main
  (:require
    [io.github.humbleui.core :as core]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window]))

(set! *warn-on-reflection* true)

(defonce *window
  (atom nil))

(defn redraw []
  (some-> @*window window/request-frame))

(def slide0
  (ui/label "Presentation title"))

(def slide1
  (ui/label "Slide 1"))

(def slide2
  (ui/label "Thank you for your attention!"))

(def slides
  [slide0
   slide1
   slide2])

(defonce *current
  (atom 0))

(add-watch *current ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(defonce *controls-visible?
  (atom false))

(defonce *controls-timer
  (atom nil))

(defn hide-controls! []
  (when-some [cancel-timer @*controls-timer]
    (cancel-timer))
  (reset! *controls-timer nil)
  (reset! *controls-visible? false)
  (redraw))

(defn show-controls! []
  (when-some [cancel-timer @*controls-timer]
    (cancel-timer))
  (reset! *controls-visible? true)
  (reset! *controls-timer (core/schedule hide-controls! 5000))
  (redraw))

(defn safe-add [x y]
  (-> (+ x y)
    (min (dec (count slides)))
    (max 0)))

(defn controls [current]
  (ui/key-listener
    {:on-key-down
     (fn [{:keys [key modifiers]}]
       (let [cmd? (modifiers :mac-command)]
         (cond
           (and cmd? (= :left key))
           (swap! *current safe-add -1000000)
                        
           (and cmd? (= :right key))
           (swap! *current safe-add 1000000)
                        
           (= :left key)
           (swap! *current safe-add -1)
                        
           (#{:right :space} key)
           (swap! *current safe-add 1))))}
    (ui/dynamic _ [controls-visible? @*controls-visible?]
      (if (not controls-visible?)
        (ui/gap 0 0)
        (ui/with-context
          {:fill-text                (paint/fill 0xFFFFFFFF)
           :hui.button/bg-active     (paint/fill 0x80000000)
           :hui.button/bg-hovered    (paint/fill 0x40000000)
           :hui.button/bg            (paint/fill 0x00000000)
           :hui.button/border-radius 0}
          (ui/clip-rrect 8
            (ui/rect
              (paint/fill 0x60000000)
              (ui/row
                (ui/width 50
                  (ui/height 50
                    (ui/button #(swap! *current safe-add -1)
                      (ui/label "◀︎"))))
                    
                (ui/width 60
                  (ui/halign 0.5
                    (ui/valign 0.5
                      (ui/label (format "%d / %d" (inc current) (count slides))))))
                      
                (ui/width 50
                  (ui/height 50
                    (ui/button #(swap! *current safe-add 1)
                      (ui/label "▶︎"))))))))))))

(def app
  (ui/default-theme
    (ui/mouse-listener
      {:on-move (fn [_] (show-controls!))
       :on-over (fn [_] (show-controls!))
       :on-out  (fn [_] (hide-controls!))}

      (ui/dynamic _ [current @*current]
        (ui/stack
          (ui/halign 0.5
            (ui/valign 0.5
              (nth slides current)))
          (ui/halign 0.5
            (ui/valign 1
              (ui/padding 0 0 0 20
                (controls current)))))))))

; (show-controls!)
(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title "Humble 🐝 Deck"
       :bg-color 0xFFFFFFFF}
      #'app)))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))