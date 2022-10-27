(ns humble-deck.speaker
  (:require
    [clojure.math :as math]
    [humble-deck.resources :as resources]
    [humble-deck.slides :as slides]
    [humble-deck.state :as state]
    [humble-deck.templates :as templates]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [java.lang AutoCloseable]))

(defn open! []
  (swap! state/*speaker-window 
    (fn [oldval]
      (or oldval
        (ui/window
          {:exit-on-close? false
           :title          "Speaker View"
           :bg-color       0xFF212B37}
          state/*speaker-app)))))

(defn close! []
  (window/close @state/*speaker-window)
  (reset! state/*speaker-window nil))

(defn toggle! []
  (if @state/*speaker-window
    (close!)
    (open!)))

(def app
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
         (close!)))}
    (ui/center
      (ui/label "Speaker view"))))

