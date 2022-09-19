(ns humble-deck.main
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [humble-deck.controls :as controls]
    [humble-deck.scaler :as scaler]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.typeface :as typeface]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.window :as window])
  (:import
    [io.github.humbleui.types IPoint IRect]
    [io.github.humbleui.skija ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

(defonce *window
  (atom nil))

(defn redraw []
  (some-> @*window window/request-frame))

(def typeface-regular
  (typeface/make-from-path "resources/CaseMicro-Regular.otf"))

(def typeface-bold
  (typeface/make-from-path "resources/CaseMicro-Bold.otf"))

(defn template-label
  ([s] (template-label {} s))
  ([opts s]
   (ui/center
     (ui/dynamic ctx [{:keys [fill-text leading]} ctx]
       (ui/column
         (interpose (ui/gap 0 leading)
           (for [line (str/split s #"\n")]
             (ui/halign 0.5
               (ui/label line)))))))))

(defn template-image [name]
  (scaler/scaler 1
    (ui/image (io/file "slides" name))))

(defn template-svg [name]
  (ui/svg (io/file "slides" name)))

(defn template-section [name]
  (ui/center
    (ui/dynamic ctx [{:keys [font-h1 leading]} ctx]
      (ui/with-context {:font-ui font-h1}
        (ui/column
          (interpose (ui/gap 0 leading)
            (for [line (str/split name #"\n")]
              (ui/halign 0.5
                (ui/label line)))))))))

(def icon-bullet
  (ui/dynamic ctx [{:keys [unit]} ctx]
    (ui/row
      (ui/valign 0.5
        (ui/rect (paint/fill 0xFF000000)
          (ui/gap (* 2 unit) (* 2 unit)))))))

(defn template-list [opts & lines]
  (let [{:keys [header range]} opts
        header-label (when header
                       (ui/dynamic ctx [{:keys [font-h1]} ctx]
                         (ui/with-context {:font-ui font-h1}
                           (ui/label header))))
        labels (map
                 #(ui/dynamic ctx [{:keys [unit]} ctx]
                    (ui/row
                      icon-bullet
                      (ui/gap (* unit 3) 0)
                      (ui/label %)))
                 lines)
        probes (concat (when header-label [header-label]) labels)]
    (for [i (if-some [[from to] range]
              (clojure.core/range from (inc to))
              [(count probes)])]
      (ui/center
        (ui/max-width probes
          (ui/dynamic ctx [{:keys [leading]} ctx]
            (ui/column
              (interpose (ui/gap 0 leading)
                (for [label (take i probes)]
                  (ui/halign 0
                    label))))))))))

(def slides
  (vec
    (flatten
      [(delay (template-svg "title.svg"))
       
       (delay (template-svg "platforms 0.svg"))
       (template-label "Web & Mobile are taken care of")
       (template-label "Unified UI is a dream")
       (template-label "Let’s focus on")
       (delay (template-svg "platforms 1.svg"))
       (template-label "How to build desktop apps?")
       
       (delay
         (ui/stack
           (template-image "native.jpg")
           (ui/dynamic ctx [{:keys [unit]} ctx]
             (ui/with-context {:font-ui (font/make-with-cap-height typeface-regular (* 50 unit))}
               (template-label "NATIVE")))))
       
       (template-list {:header "Native is" :range [1 2]} "$" "Organizationally Complicated")
       (template-list {:header "Native is" :range [2 2]} "$$" "Organizationally Complicated")
       (template-list {:header "Native is" :range [2 2]} "$$$" "Organizationally Complicated")
       (template-list {:header "Native is" :range [2 2]} "$$$$" "Organizationally Complicated")
       (template-list {:header "Native is" :range [2 2]} "$$$$ Expensive" "Organizationally Complicated")
       (template-list {:header "Native is" :range [3 4]} "$$$$ Expensive" "Organizationally Complicated" "Kinda pointless")

       (delay
         (ui/rect (paint/fill 0xFF2C2E3A)
           (template-svg "electron.svg")))
       (template-list {:header "Downsides:" :range [2 5]} "Threading model" "JS Performance" "DOM Performance" "No low-level control")
       
       (delay
         (ui/stack
           (template-image "electron_apps.jpg")
           (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
             (template-label "1.\nPeople WANT apps"))))
       
       (template-label "2.\nPeople are OK\nwith non-native UI")

       (delay (template-svg "buttons 0.svg"))
       (delay (template-svg "buttons 1.svg"))
       (template-label "3.\nThere is no good alternative")
       
       (template-label "QT")
       (template-label "QT\nis C++")
       
       (template-label "Java UIs")
       (template-label "Java UIs\nare cursed 👻")
       (template-label "But they don’t have to be!")
       
       (template-section "Let’s build a UI framework!")
       
       (template-list {:range [1 5]}
         "For desktop apps"
         "Take place of Electron"
         "Clojure-centric"
         "JVM-only, no JavaScript")

       (template-list {:header "Why Clojure?" :range [1 4]}
         "Dynamic"
         "Fast"
         "REPL")
       
       (template-section "REPL + UI is a match\nmade in heaven")
       (template-list {:header "REPL + UI" :range [2 5]}
         "instant feedback loop"
         "play & experiment"
         "like figwheel/shadow-cljs"
         "but without browser")
       
       


       ])))

(defonce *current
  (atom 0))

(add-watch *current ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(add-watch controls/*controls-visible? ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(def app
  (ui/with-bounds ::bounds
    (ui/dynamic ctx [cap-height (-> (::bounds ctx) :height (quot 10))]
      (let [font-body (font/make-with-cap-height typeface-regular cap-height)
            font-h1 (font/make-with-cap-height typeface-bold cap-height)]
        (ui/default-theme
          {:face-ui typeface-regular}
          (ui/with-context
            {:font-body font-body
             :font-h1   font-h1
             :leading   (quot cap-height 2)
             :unit      (quot cap-height 10)
             :fill-text (paint/fill 0xFF212B37)}
            (ui/mouse-listener
              {:on-move (fn [_] (controls/show-controls!))
               :on-over (fn [_] (controls/show-controls!))
               :on-out  (fn [_] (controls/hide-controls!))}
              (ui/stack
                (ui/rect (paint/fill 0xFFFFFFFF)
                  (ui/dynamic ctx [{:keys [font-body]} ctx
                                   current @*current]
                    (ui/with-context
                      {:font-ui font-body}
                      (let [slide (nth slides current)]
                        (cond-> slide
                          (delay? slide) deref)))))
                (ui/halign 0.5
                  (ui/valign 1
                    (ui/padding 0 0 0 20
                      (controls/controls *current slides))))))))))))

(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title    "Desktop UI with Clojure"
       :mac-icon "resources/icon.icns"
       :bg-color 0xFFFFFFFF}
      #'app))
  (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @*window)) (ColorSpace/getDisplayP3))
  (redraw))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))