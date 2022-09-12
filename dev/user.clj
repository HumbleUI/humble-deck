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
      (window/set-window-position window 2260 76)
      (window/set-window-size window 764 1312)
      (window/set-z-order window :floating)))
  (nrepl/-main "--interactive"))