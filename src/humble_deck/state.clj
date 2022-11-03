(ns ^{:clojure.tools.namespace.repl/load false}
  humble-deck.state)

(def *slides
  (atom nil))

(def *app
  (atom nil))

(def *window
  (promise))

(def *speaker-window
  (atom nil))

(def *speaker-app
  (atom nil))

(def *state
  (atom
    {:slide           0
     :subslide        0
     :mode            :present
     :animation-start nil
     :animation-end   nil
     :controls?       true
     :controls-timer  nil
     :speaker-timer   nil}))

(def *slider
  (atom
    {:value 0
     :min   0}))

(def *image-snapshot?
  (atom true))