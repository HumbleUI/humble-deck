(ns user
  (:require
    [humble-deck.main :as main]
    [nrepl.cmdline :as nrepl]
    ))

(defn -main [& args]
  (main/-main)
  (nrepl/-main "--interactive"))