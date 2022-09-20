(ns humble-deck.scaler
  (:require
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.protocols :as protocols])
  (:import
    [io.github.humbleui.types IPoint IRect]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

(core/deftype+ Scaler [child]
  protocols/IComponent
  (-measure [_ ctx cs]
    (let [child-size (core/measure child ctx cs)
          ratio      (/ (:width child-size) (:height child-size))]
      (IPoint.
        (min (:width cs) (* (:height cs) ratio))
        (min (:height cs) (/ (:width cs) ratio)))))
  
  (-draw [_ ctx rect ^Canvas canvas]
    (let [child-size (core/measure child ctx (IPoint. (:width rect) (:height rect)))
          ratio      (/ (:width child-size) (:height child-size))
          width'     (min (:width rect) (* (:height rect) ratio))
          height'    (min (:height rect) (/ (:width rect) ratio))
          rect'      (IRect/makeXYWH
                       (+ (:x rect) (/ (- (:width rect) width') 2))
                       (+ (:y rect) (/ (- (:height rect) height') 2))
                       width'
                       height')]
      (core/draw-child child ctx rect' canvas)))
  
  (-event [_ ctx event]
    (core/event-child child ctx event))
  
  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))
  
  AutoCloseable
  (close [_]
    (core/child-close child)))

(defn scaler [child]
  (->Scaler child))