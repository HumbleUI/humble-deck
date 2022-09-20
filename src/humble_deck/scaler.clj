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
    (let [width'  (min (:width cs) (* (:height cs) ratio))
          height' (min (:height cs) (/ (:width cs) ratio))]
      (IPoint. width' height')))
  
  (-draw [_ ctx rect ^Canvas canvas]
    (let [width'  (min (:width rect) (* (:height rect) ratio))
          height' (min (:height rect) (/ (:width rect) ratio))
          rect'   (IRect/makeXYWH
                    (+ (:x rect) (/ (- (:width rect) width') 2))
                    (+ (:y rect) (/ (- (:height rect) height') 2))
                    width'
                    height')
          
          child-size  (core/measure child ctx (IPoint. (:width rect') (:height rect')))
          child-ratio (/ (:width child-size) (:height child-size))
          child-w     (min (:width rect') (* (:height rect') child-ratio))
          child-h     (min (:height rect') (/ (:width rect') child-ratio))
          child-rect  (IRect/makeXYWH
                        (+ (:x rect') (/ (- (:width rect') child-w) 2))
                        (+ (:y rect') (/ (- (:height rect') child-h) 2))
                        child-w
                        child-h)]
      (core/draw-child child ctx child-rect canvas)))
  
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