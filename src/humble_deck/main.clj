(ns humble-deck.main
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [humble-deck.controls :as controls]
    [humble-deck.scaler :as scaler]
    [io.github.humbleui.canvas :as canvas]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
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

(defonce *mode
  (atom :overview))

(add-watch *current ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(add-watch *mode ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(add-watch controls/*controls-visible? ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(def overview-padding
  10)

(defn slide-size [{:keys [width height]}]
  (let [per-row (max 1 (quot width 200))
        slide-w (-> width
                  (- (* (inc per-row) overview-padding))
                  (quot per-row))
        ratio   (/ 3024 1964) #_(/ width height)]
    {:per-row per-row
     :slide-w slide-w
     :slide-h (/ slide-w ratio)}))
     

(def overview
  (ui/rect (paint/fill 0xFFF0F0F0)
    (ui/with-bounds ::bounds
      (ui/dynamic ctx [{:keys [scale]} ctx
                       {:keys [per-row slide-w slide-h]} (slide-size (::bounds ctx))]
        (ui/vscrollbar
          (ui/vscroll
            (ui/padding 10
              (let [cap-height (quot slide-h 10)
                    font-body  (font/make-with-cap-height typeface-regular cap-height)
                    font-h1    (font/make-with-cap-height typeface-bold cap-height)
                    full-len   (-> (count slides) (dec) (quot per-row) (inc) (* per-row))
                    slides'    (concat slides (repeat (- full-len (count slides)) nil))]
                (ui/with-context
                  {:font-body font-body
                   :font-h1   font-h1
                   :font-ui   font-body
                   :leading   (quot cap-height 2)
                   :unit      (quot cap-height 10)}
                  (ui/column
                    (interpose (ui/gap 0 overview-padding)
                      (for [row (partition per-row (core/zip (range) slides'))]
                        (ui/height slide-h
                          (ui/row
                            (interpose (ui/gap overview-padding 0)
                              (for [[idx slide] row
                                    :let [slide-comp (ui/rect (paint/fill 0xFFFFFFFF)
                                                       (if (delay? slide) @slide slide))]]
                                [:stretch 1
                                 (when slide
                                   (ui/clickable
                                     {:on-click
                                      (fn [_]
                                        (reset! *mode :present)
                                        (reset! *current idx))}
                                     (ui/clip-rrect 4
                                       (ui/dynamic ctx [{:hui/keys [hovered?]} ctx]
                                         (if hovered?
                                           (ui/stack
                                             slide-comp
                                             (ui/rect (paint/fill 0x20000000)
                                               (ui/gap 0 0)))
                                           slide-comp)))))]))))))))))))))))

(def slide
  (ui/stack
    (ui/with-bounds ::bounds
      (ui/dynamic ctx [cap-height (-> (::bounds ctx) :height (quot 10))]
        (let [font-body (font/make-with-cap-height typeface-regular cap-height)
              font-h1   (font/make-with-cap-height typeface-bold cap-height)]
          (ui/with-context
            {:font-body font-body
             :font-h1   font-h1
             :font-ui   font-body
             :leading   (quot cap-height 2)
             :unit      (quot cap-height 10)}
            (ui/rect (paint/fill 0xFFFFFFFF)
              (ui/dynamic _ [current @*current]
                (let [slide (nth slides current)]
                  (cond-> slide
                    (delay? slide) deref))))))))
    (ui/mouse-listener
      {:on-move (fn [_] (controls/show-controls!))
       :on-over (fn [_] (controls/show-controls!))
       :on-out  (fn [_] (controls/hide-controls!))}
      (ui/halign 0.5
        (ui/valign 1
          (ui/padding 0 0 0 20
            (controls/controls *current *mode slides)))))))

(def app
  (ui/default-theme {:face-ui typeface-regular}
    (ui/with-context {:fill-text (paint/fill 0xFF212B37)}
      (ui/dynamic _ [mode @*mode]
        (case mode
          :overview overview
          :present  slide)))))

(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title    "Desktop UI with Clojure"
       :mac-icon "resources/icon.icns"
       :bg-color 0xFFFFFFFF}
      #'app))
  (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @*window)) (ColorSpace/getDisplayP3))
  (reset! debug/*enabled? true)
  (redraw))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))