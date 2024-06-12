(ns user
  (:require
    [clj-reload.core :as reload]
    [duti.core :as duti]))

(reload/init
  {; :dirs ["src" "decks/2024-06-clojure-berlin"]
   :no-reload '#{user}})

(defn reload []
  (reload/reload))

(defn -main [& args]
  (require 'humble-deck.main)
  (apply duti/-main args)
  (@(resolve 'humble-deck.main/-main)))
