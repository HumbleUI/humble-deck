(ns humble-deck.scaler
  (:require
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.protocols :as protocols])
  (:import
    [io.github.humbleui.types IPoint IRect]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

(core/deftype+ Scaler [child ^:mut child-rect]
  protocols/IComponent
  (-measure [_ ctx cs]
    (let [child-size (core/measure child ctx cs)
          scale-w    (/ (:width cs) (:width child-size))
          scale-h    (/ (:height cs) (:height child-size))
          scale      (min scale-w scale-h)]
      (IPoint.
        (* scale (:width child-size))
        (* scale (:height child-size)))))
  
  (-draw [_ ctx rect ^Canvas canvas]
    (let [child-size (core/measure child ctx (IPoint. (:width rect) (:height rect)))
          scale-w    (/ (:width rect) (:width child-size))
          scale-h    (/ (:height rect) (:height child-size))
          scale      (min scale-w scale-h)]
      (set! child-rect (IRect/makeXYWH (:x rect) (:y rect) (* scale (:width child-size)) (* scale (:height child-size))))
      (canvas/with-canvas canvas
        (canvas/translate canvas (:x rect) (:y rect))
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

(defn scaler [child]
  (->Scaler child nil))