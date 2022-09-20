(ns user
  (:require
    [humble-deck.core :refer :all]
    [humble-deck.main :as main]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.window :as window]
    [nrepl.cmdline :as nrepl]))

(defn reset-window []
  (app/doui
    (when-some [window @*window]
      (window/set-window-position window 860 566)
      (window/set-content-size window 1422 800)
      #_(window/set-z-order window :floating))))

(defn -main [& args]
  (main/-main)
  ; (reset! debug/*enabled? true)
  (reset-window)
  (nrepl/-main "--interactive"))

(comment
  (reset-window)
  (app/doui
    (window/window-rect @*window))
  (app/doui
    (window/set-z-order @*window :normal)))