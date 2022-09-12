(ns humble-deck.main
  (:require
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window]))

(set! *warn-on-reflection* true)

(defonce *window
  (atom nil))

(def app
  (ui/default-theme
    (ui/halign 0.5
      (ui/valign 0.5
        (ui/label "Humble Deck")))))

(some-> @*window window/request-frame)

(defn -main [& args]
  (reset! *window 
    (ui/start-app! {:title "Humble 🐝 Deck"} #'app)))

(comment
  (some-> @*window window/request-frame))