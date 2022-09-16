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

(defn template-image [name & label]
  (scaler/scaler 1
    (ui/image (io/file "slides" name))))

(defn template-svg [name]
  (scaler/scaler 1
    (ui/svg (io/file "slides" name))))

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
       
       (template-section "Let’s focus on\nDESKTOP")
       (template-label "Mobile is taken care of")
       (template-label "Mobile-desktop unification\nis a dream")
       
       (template-section "Time for desktop apps is NOW")
       (delay
         (ui/stack
           (template-image "electron_apps.jpg")
           (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
             (template-label "People love apps"))))
       (template-label "Native?")
       (template-label "It’s hard to make a case for")
       (template-list {:range [1 5]} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Stupid")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Suboptimal")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Suboptimal" "And $")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Suboptimal" "And $$")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Suboptimal" "And $$$")
       (template-list {} "Multiple platforms" "Multiple teams" "Separate codebases" "Same app" "Suboptimal" "And $$$$")
       (template-label "And")
       (template-label "And Logistically")
       (template-label "And Logistically Complicated")
       (template-label "Frankly, not worth it")
       (delay (template-svg "ui_quality.svg"))
       (template-label "People are accustomed\nto cross-platform UIs")
       (delay (template-svg "affordances 0.svg"))
       (delay (template-svg "affordances 1.svg"))
       (delay (template-svg "affordances 2.svg"))
       (delay (template-svg "affordances 3.svg"))
       (delay (template-svg "affordances 4.svg"))
       (delay (template-svg "affordances 5.svg"))
       (delay (template-svg "affordances 6.svg"))
       (delay (template-svg "affordances 7.svg"))
       
       (template-section "So")
       (template-list {:header "We want" :range [1 4]} "UI framework" "desktop" "cross-platform")
       
       (template-section "Electron!")
       (template-list {:header "Electron:" :range [2 4]} "Performance" "Resource utilization" "Threading model")
       
       (template-label "QT")
       (template-label "QT is C++")
       (template-label "Java UI")
       (template-label "Java UI is cursed")
       (template-label "Swing is too old")
       (template-label "JavaFX is too quirky")
       
       (template-label "There’s no inherent reason\nwhy Java UI won’t work")
       (template-label "It just hasn’t been done yet")
       
       
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
            {:font-body  font-body
             :font-h1    font-h1
             :leading    (quot cap-height 2)
             :unit       (quot cap-height 10)}
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
      #'app)))

(comment
  (redraw)
  (reset! *current 2)
  (window/window-rect @*window))