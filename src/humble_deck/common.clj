(ns humble-deck.common
  (:require
    [humble-deck.resources :as resources]
    [humble-deck.state :as state]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window]))

(defn maybe-deref [ref]
  (cond-> ref
    (instance? clojure.lang.IDeref ref) deref))

(defn redraw []
  (some-> state/*window         deref window/request-frame)
  (some-> state/*speaker-window deref window/request-frame)
  :success)

(defn speaker-open! []
  (swap! state/*state
    assoc :speaker-timer (core/schedule #(some-> state/*speaker-window deref window/request-frame) 0 1000))
  (swap! state/*speaker-window
    (fn [oldval]
      (or oldval
        (ui/window
          {:exit-on-close? false
           :title          "Speaker View"
           :bg-color       0xFF212B37}
          state/*speaker-app)))))

(defn speaker-close! []
  ((:speaker-timer @state/*state))
  (swap! state/*state dissoc :speaker-timer)
  (window/close @state/*speaker-window)
  (reset! state/*speaker-window nil))

(defn speaker-toggle! []
  (if @state/*speaker-window
    (speaker-close!)
    (speaker-open!)))

(defn slide-prev [state]
  (cond
    (> (:subslide state) 0)
    (update state :subslide dec)
                                      
    (> (:slide state) 0)
    (-> state
      (update :slide dec)
      (assoc :subslide (dec (count (nth @state/*slides (dec (:slide state)))))))))

(defn slide-next [state]
  (cond
    (< (:subslide state) (dec (count (nth @state/*slides (:slide state)))))
    (update state :subslide inc)
                                  
    (< (:slide state) (dec (count @state/*slides)))
    (-> state
      (update :slide inc)
      (assoc :subslide 0))))

(defn slide-prev-10 [state]
  (assoc state
    :slide    (max 0 (- (:slide state) 10))
    :subslide 0))

(defn slide-next-10 [state]
  (assoc state
    :slide    (min
                (dec (count @state/*slides))
                (- (:slide state) 10))
    :subslide 0))

(defn slide-first [state]
  (assoc state
    :slide 0
    :subslide 0))

(defn slide-last [state]
  (assoc state
    :slide    (dec (count @state/*slides))
    :subslide (dec (count (peek @state/*slides)))))

(defn with-context
  ([child]
   (with-context {} child))
  ([opts child]
   (ui/with-bounds ::bounds
     (ui/dynamic ctx [scale      (:scale ctx)
                      cap-height (or (some-> (:cap-height opts) (* scale))
                                   (-> ctx ::bounds :height (* scale) (quot 30)))]
       (let [font-body (font/make-with-cap-height resources/typeface-regular cap-height)
             font-h1   (font/make-with-cap-height resources/typeface-bold    cap-height)
             font-code (font/make-with-cap-height resources/typeface-code    cap-height)]
         (ui/default-theme {:face-ui resources/typeface-regular}
           (ui/with-context
             (merge
               {:face-ui   resources/typeface-regular
                :font-body font-body
                :font-h1   font-h1
                :font-code font-code
                :leading   (quot cap-height 2)
                :fill-text (paint/fill 0xFF212B37)
                :unit      (quot cap-height 10)}
               opts)
             child)))))))

(def slide
  (ui/rect (paint/fill 0xFFFFFFFF)
    (ui/dynamic _ [{:keys [slide subslide]} @state/*state]
      (-> @state/*slides (nth slide) (nth subslide) maybe-deref))))