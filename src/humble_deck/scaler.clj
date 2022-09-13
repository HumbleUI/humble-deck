(ns humble-deck.scaler
  (:require
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.protocols :as protocols])
  (:import
    [io.github.humbleui.types IPoint IRect]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

(core/deftype+ Scaler [ratio child ^:mut child-rect]
  protocols/IComponent
  (-measure [_ ctx cs]
    (let [child-size (core/measure child ctx cs)
          scale-w    (-> (:width cs) (/ (:width child-size)) double)
          scale-h    (-> (:height cs) (/ (:height child-size)) double)
          scale      (min scale-w scale-h)]
      (IPoint.
        (* scale (:width child-size))
        (* scale (:height child-size)))))
  
  (-draw [_ ctx rect ^Canvas canvas]
    (let [cs         (IPoint. (:width rect) (:height rect))
          child-size (core/measure child ctx cs)
          scale-w    (-> (:width cs) (/ (:width child-size)))
          scale-h    (-> (:height cs) (/ (:height child-size)))
          scale      (* ratio (min scale-w scale-h))
          rect'      (IRect/makeXYWH
                       (+ (:x rect) (/ (- (:width rect) (* scale (:width child-size))) 2))
                       (+ (:y rect) (/ (- (:height rect) (* scale (:height child-size))) 2))
                       (* scale (:width child-size))
                       (* scale (:height child-size)))]
      (set! child-rect rect')
      (canvas/with-canvas canvas
        (canvas/translate canvas (:x rect') (:y rect'))
        (canvas/scale canvas scale)
        (core/draw-child child ctx (IRect/makeXYWH 0 0 (:width child-size) (:height child-size)) canvas))))
  
  (-event [_ ctx event]
    (core/event-child child ctx event))
  
  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))
  
  AutoCloseable
  (close [_]
    (core/child-close child)))

(defn scaler [ratio child]
  (->Scaler ratio child nil))