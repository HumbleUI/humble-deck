(ns ^{:clojure.tools.namespace.repl/load false}
  user
  (:require
    [clojure.core.server :as server]
    [clojure.java.io :as io]
    [clojure.test :as t]
    [clojure.tools.namespace.repl :as ns]
    [clojure.tools.namespace.track :as track]
    [humble-deck.main :as main]
    [humble-deck.state :as state]
    [io.github.humbleui.app :as app]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.window :as window]))

(ns/disable-reload!)

(ns/set-refresh-dirs "src")

(def *reloaded
  (atom nil))

(add-watch #'ns/refresh-tracker ::watch
  (fn [_ _ old new]
    (when (empty? (::track/load new))
      (reset! *reloaded (::track/load old)))))

(defn after-reload []
  (load-file (str "decks/" state/deck "/slides.clj"))
  (reset! state/*slides @(resolve 'slides/slides))
  (swap! state/*state update :epoch inc)
  (@(requiring-resolve 'humble-deck.common/redraw))
  (let [cnt (count @*reloaded)]
    (str "Reloaded " cnt " namespace" (when (> cnt 1) "s"))))

(defn reload []
  (clojure.tools.namespace.reload/remove-lib 'slides)
  (set! *warn-on-reflection* true)
  (let [res (ns/refresh :after 'user/after-reload)]
    (if (instance? Throwable res)
      (throw res)
      res)))

(def p-lock
  (Object.))

(defn p-pos []
  (let [trace (->> (Thread/currentThread)
                (.getStackTrace)
                (seq))
        el    ^StackTraceElement (nth trace 4)]
    (str "[" (clojure.lang.Compiler/demunge (.getClassName el)) " " (.getFileName el) ":" (.getLineNumber el) "]")))

(defn p-impl [position form res]
  (let [form (clojure.walk/postwalk
               (fn [form]
                 (if (and
                       (list? form)
                       (= 'user/p-impl (first form)))
                   (clojure.lang.TaggedLiteral/create 'p (nth form 3))
                   form))
               form)]
    (locking p-lock
      (println (str position " #p " form " => " (pr-str res))))
    res))

(defn p [form]
  `(p-impl (p-pos) '~form ~form))

(defn -main [& args]
  (apply main/-main args)

  ;; setup repl
  (let [args (apply array-map args)
        port (or
               (some-> (get args "--port") parse-long)
               (+ 1024 (rand-int 64512)))
        file (io/file ".repl-port")]
    (println "Started Server Socket REPL on port" port)
    (spit file port)
    (.deleteOnExit file)
    (server/start-server
      {:name          "repl"
       :port          port
       :accept        'clojure.core.server/repl
       :server-daemon false})))

(comment
  (app/doui
    (window/set-z-order @state/*window :floating)))