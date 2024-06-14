(ns humble-deck.common
  (:require
    [clojure.math :as math]
    [humble-deck.resources :as resources]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [java.time Duration Instant LocalTime ZoneId]
    [java.time.format DateTimeFormatter]))

(defonce ^:private uuid
  (random-uuid))

(defonce *slides
  (atom nil))

(defonce *window
  (atom nil))

(defonce *speaker-window
  (atom nil))

(defonce *speaker-app
  (atom nil))

(defonce *state
  (atom
    {:slide           0
     :subslide        0
     :mode            :present
     :animation-start nil
     :animation-end   nil
     :controls?       true
     :controls-timer  nil
     :speaker-timer   nil
     :speaker-time    0
     :speaker-start   nil
     :epoch           0}))

(defonce *slider
  (atom
    {:value 0
     :min   0}))

(defonce *image-snapshot?
  (atom true))

(defonce *talk-duration
  (atom (* 40 60 1000)))

(defn maybe-deref [ref]
  (cond-> ref
    (instance? clojure.lang.IDeref ref) deref))

(defn time-format-HH-mm [ms]
  (let [i (Instant/ofEpochMilli ms)
        t (LocalTime/ofInstant i (ZoneId/systemDefault))]
    (.format t (DateTimeFormatter/ofPattern "HH:mm"))))

(defn duration-format-mm-ss [ms]
  (let [d (Duration/ofMillis ms)]
    (format "%02d:%02d" (.toMinutes d) (.toSecondsPart d))))

(defn redraw []
  (some-> *window         deref window/request-frame)
  (some-> *speaker-window deref window/request-frame)
  :success)

(add-watch *state ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(defmacro template-icon-button [icon & on-click]
  `(ui/button (fn [] ~@on-click) 
     (ui/width 40
       (ui/height 40
         ~icon))))

(defn speaker-open! []
  (swap! *state
    assoc :speaker-timer (core/schedule #(some-> *speaker-window deref window/request-frame) 0 1000))
  (swap! *speaker-window
    (fn [oldval]
      (or oldval
        (ui/window
          {:exit-on-close? false
           :title          "Speaker View"
           :bg-color       0xFF212B37}
          *speaker-app)))))

(defn speaker-close! []
  ((:speaker-timer @*state))
  (swap! *state dissoc :speaker-timer)
  (window/close @*speaker-window)
  (reset! *speaker-window nil))

(defn speaker-toggle! []
  (if @*speaker-window
    (speaker-close!)
    (speaker-open!)))

(defn slide-prev [state]
  (cond
    (> (:subslide state) 0)
    (update state :subslide dec)
                                      
    (> (:slide state) 0)
    (-> state
      (update :slide dec)
      (assoc :subslide (dec (count (nth @*slides (dec (:slide state)))))))))

(defn slide-next [state]
  (cond
    (< (:subslide state) (dec (count (nth @*slides (:slide state)))))
    (update state :subslide inc)
                                  
    (< (:slide state) (dec (count @*slides)))
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
                (dec (count @*slides))
                (- (:slide state) 10))
    :subslide 0))

(defn slide-first [state]
  (assoc state
    :slide 0
    :subslide 0))

(defn slide-last [state]
  (assoc state
    :slide    (dec (count @*slides))
    :subslide (dec (count (peek @*slides)))))

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
                :leading   (-> cap-height (/ scale) (math/ceil))
                :fill-text (paint/fill 0xFF212B37)
                :unit      (-> cap-height (/ scale) (/ 10) (math/ceil))}
               opts)
             child)))))))

(def slide
  (ui/rect (paint/fill 0xFFFFFFFF)
    (ui/dynamic _ [{:keys [slide subslide epoch]} @*state]
      (-> @*slides (nth slide) (nth subslide) maybe-deref))))
