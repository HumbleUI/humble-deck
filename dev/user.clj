(ns ^{:clojure.tools.namespace.repl/load false}
  user
  (:require
    [humble-deck.main :as main]
    [humble-deck.state :as state]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.window :as window]
    [nrepl.cmdline :as nrepl]
    [clojure.tools.namespace.repl :as ns]))

(defn reset-window []
  (app/doui
    (when-some [window @state/*window]
      (window/set-window-position window 860 566)
      (window/set-content-size window 1422 800)
      #_(window/set-z-order window :floating))))

(defn reload []
  (let [res (ns/refresh :after 'humble-deck.common/redraw)]
    (if (instance? Throwable res)
      (throw res)
      res)))

(defn -main [& args]
  (ns/set-refresh-dirs "src")
  (main/-main)
  ; (reset! debug/*enabled? true)
  (reset-window)
  (nrepl/-main "--interactive"))

(comment
  (reload)
  (reset-window)
  (app/doui
    (window/window-rect @state/*window))
  (app/doui
    (window/set-z-order @state/*window :normal)))
