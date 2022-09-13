(ns user
  (:require
    [humble-deck.main :as main]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.window :as window]
    [nrepl.cmdline :as nrepl]))

(defn -main [& args]
  (main/-main)
  ; (reset! debug/*enabled? true)
  (app/doui
    (when-some [window @main/*window]
      (window/set-window-position window 1976 76)
      (window/set-window-size window 1048 644)
      (window/set-z-order window :floating)))
  (nrepl/-main "--interactive"))

(comment
  (app/doui
    (window/window-rect @main/*window))
  (app/doui
    (window/set-z-order @main/*window :normal)))