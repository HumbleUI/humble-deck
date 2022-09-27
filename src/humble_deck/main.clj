(ns humble-deck.main
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [humble-deck.controls :as controls]
    [humble-deck.core :refer :all]
    [humble-deck.overview :as overview]
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
    [io.github.humbleui.skija Color ColorSpace]
    [io.github.humbleui.jwm Window]
    [io.github.humbleui.jwm.skija LayerMetalSkija]
    [java.lang AutoCloseable]))

(set! *warn-on-reflection* true)

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

(defn template-code
  ([s] (template-code {} s))
  ([opts s]
   (ui/center
     (ui/dynamic ctx [{:keys [fill-text leading font-code]} ctx]
       (ui/with-context
         {:font-ui font-code}
         (ui/column
           (interpose (ui/gap 0 (* 1.5 leading))
             (for [line (str/split s #"\n")]
               (ui/halign 0
                 (ui/label line))))))))))

(defn template-image [name]
  (scaler/scaler
    (ui/image (io/file "slides" name))))

(defn template-svg [name]
  (scaler/scaler
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

(def *counter
  (atom 1))

(def *checkbox
  (atom true))

(def *slider
  (atom {:value 50 :min 0 :max 100}))

(def *text
  (atom {:text ""
         :placeholder "Edit me"}))

(def demo
  (ui/dynamic ctx [{:keys [font-default unit scale]} ctx]
    (let [unit (* 1 scale)]
    (ui/with-context
      {:font-ui font-default}
      (ui/center
        (ui/column
          (ui/halign 0.5
            (ui/dynamic _ [counter @*counter]
              (ui/label (str "Clicks: " counter))))
          (ui/gap 0 (* unit 5))
          
          (ui/halign 0.5
            (ui/button (fn [] (swap! *counter inc) (redraw))
              (ui/label "Click me")))
          (ui/gap 0 (* unit 5))
          
          (ui/halign 0.5
            (ui/width (* unit 30)
              (ui/text-field *text)))
          (ui/gap 0 (* unit 5))
          
          (ui/halign 0.5
            (ui/checkbox *checkbox
              (ui/label "Check me")))
          (ui/gap 0 (* unit 5))
          
          (ui/halign 0.5
            (ui/row
              (ui/valign 0.5
                (ui/toggle *checkbox))
              (ui/gap (* unit 1) 0)
              (ui/clickable
                {:on-click (fn [_] (swap! *checkbox not))}
                (ui/valign 0.5
                  (ui/label "Toggle me")))))
          (ui/gap 0 (* unit 5))
          
          (ui/halign 0.5
            (ui/row
              (ui/width (* unit 30)
                (ui/slider *slider))
              (ui/gap (* unit 2) 0)
              (ui/width (* unit 5)
                (ui/halign 1
                  (ui/dynamic _ [value (:value @*slider)]
                    (ui/label value))))))
            
          ))))))

(def debug
  (ui/dynamic ctx [{:keys [unit]} ctx]
    (ui/center
      (ui/row
        (ui/valign 0.5
          (ui/toggle debug/*enabled?))
        (ui/gap (* unit 2) 0)
        (ui/valign 0.5
          (ui/label "Debug info"))))))

(def slides
  (vec
    (flatten
      [(delay (template-svg "title.svg"))
       
       (delay (template-svg "platforms 0.svg"))
       (delay (template-svg "platforms 1.svg"))
       (delay (template-svg "platforms 2.svg"))
       (delay (template-svg "platforms 3.svg"))
       
       (delay
         (ui/stack
           (template-image "native.jpg")
           (template-section "NATIVE")))
       
       (template-list {:header "Native is" :range [1 4]} "$$$$" "Organizationally Complicated" "Kinda pointless")

       (template-label "QT")
       (template-label "QT\nis C++")

       (template-label "Java UIs")
       (template-label "Java UIs\nare cursed 👻")
      
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

       (delay
         (ui/rect (paint/fill 0xFFF3F3F3)
           (template-svg "buttons 0.svg")))
       (delay (template-image "buttons 1.jpg"))
       (template-label "3.\nThere is no good alternative")
       
       (delay (template-svg "humbleui.svg"))
       
       (template-list {:range [1 8] :header "Humble UI"}
         "UI Framework"
         "For desktop apps"
         "~ Electron"
         "No browser"
         "No DOM"
         "No JS"
         "JVM Clojure only")
       
       (template-section "DEMO")
       
       demo
       
       (template-section "Anatomy of UI framework")
       
       (delay (template-svg "architecture.svg"))
       
       (delay (template-svg "skia.svg"))
       
       (template-list {:header "Skia" :range [1 10]}
         "Graphics library"
         "Good enough for Chrome"
         "& Android"
         "& Flutter, Libre Office, Avalonia, ..."
         "Fast"
         "Modern"
         "OpenGL"
         "DirectX 11, 12"
         "Metal, Vulkan, WebGPU")
       
       debug
       
       (delay (template-svg "skija.svg"))
       
       (template-list {:header "Skija"}
         "Skia bindings for Java")
       
       (delay (template-svg "jwm.svg"))
       
       (template-list {:header "JWM" :range [1 7]}
         "Java Window Management"
         "OS integration"
         "Modern capabilities"
         "Multi-monitor, VSync, Color Profiles, HiDPI"
         "High-quality"
         "Indistinguishable from native")
       
       (delay (template-svg "architecture.svg"))
       
       (template-section "Killer features")
       
       (template-list {:header "Simple layout" :range [1 3]}
         "Parent imposes size"
         "Child chooses their own size")
       (template-list {:header "Simple layout" :range [3 3]}
         "(-draw child size)"
         "(-measure child space)"
         "Child chooses their own size")
       
       (template-section "Composable components")
       (delay (template-image "attributes.jpg"))
       (delay (template-svg "buttons 2.svg"))
       (template-code
         "(defn button [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
      (clip-rrect (:radius opts)
        (rect (:fill opts)
          (padding (:padding opts)
            (center
              (label (:caption opts)))))))))")
       (template-code
         "(defn button [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
      (clip-diagonal (:radius opts)
        (rect (:fill opts)
          (padding (:padding opts)
            (center
              (label (:caption opts)))))))))")
       (template-code
         "(defn button [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
      (shadow (:shadow opts)
        (clip-diagonal (:radius opts)
          (rect (:fill opts)
            (padding (:padding opts)
              (center
                (label (:caption opts))))))))))")
       
       (template-section "Sane text metrics")
       (delay (template-svg "capsize 0.svg"))
       (delay (template-svg "capsize 1.svg"))
       (delay (template-svg "capsize 2.svg"))
       (delay (template-svg "capsize 3.svg"))
       
       (template-list {:header "Also" :range [1 7]}
         "Wide Color Gamut"
         "OpenType features"
         "OkLab Gradients"
         "Squircles"
         "Pixel-perfect scaled graphics"
         "...")

       (template-list {:header "Clojure" :range [1 5]}
         "❤️"
         "High-level"
         "Easy to use"
         "Fast"
         "Fast(er than JS)")
       
       (template-list {:header "Clojure" :range [5 7]}
         "❤️"
         "High-level"
         "Easy to use"
         "Fast(er than JS)"
         "Threads"
         "REPL")
       
       ;; (ui/rect (paint/fill 0xFF00EEEE)
       (template-section "REPL + UI\n=\n💪🦸‍♂️🦸‍♀️🤳\nSUPERPOWER")
       
       (template-list {:header "REPL + UI" :range [2 5]}
         "Instant Feedback"
         "play & experiment"
         "like figwheel/shadow-cljs"
         "but without browser")
       
       (template-list {:header "Status" :range [1 4]}
         "Active development"
         "Pre-alpha"
         "Everything changes"
         "Everything changes. A lot")
       
       (template-list {:header "Status" :range [4 4]}
         "Active development"
         "Pre-alpha"
         "Everything changes. A lot")
       
       (template-list {:header "Missing pieces" :range [1 7]}
         "JWM / OS integration (Java, C++)"
         "State management"
         "Rich text"
         "Viewports"
         "Distribution"
         "Testing and adoption")
       
       (delay (template-svg "links.svg"))
       
       (template-label "Thank you!")
       
       (template-label "Questions?")
       ])))

(add-watch *state ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(add-watch controls/*controls-visible? ::redraw
  (fn [_ _ old new]
    (when (not= old new)
      (redraw))))

(def slide
  (ui/stack
    (ui/with-bounds ::bounds
      (ui/dynamic ctx [scale (:scale ctx)
                       cap-height (-> ctx ::bounds :height (* scale) (quot 30))]
        (let [font-default (font/make-with-cap-height typeface-regular (* scale 10))
              font-body    (font/make-with-cap-height typeface-regular cap-height)
              font-h1      (font/make-with-cap-height typeface-bold    cap-height)
              font-code    (font/make-with-cap-height typeface-code    cap-height)]
          (ui/with-context
            {:font-default font-default
             :font-body font-body
             :font-h1   font-h1
             :font-ui   font-body
             :font-code font-code
             :leading   (quot cap-height 2)
             :unit      (quot cap-height 10)}
            (ui/rect (paint/fill 0xFFFFFFFF)
              (ui/dynamic _ [current (:current @*state)]
                (let [slide (nth slides current)]
                  (cond-> slide
                    (delay? slide) deref))))))))
    (controls/controls *state slides)))

(def app
  (ui/default-theme {:face-ui typeface-regular}
    (ui/with-context {:fill-text (paint/fill 0xFF212B37)}
      (controls/key-listener *state slides
        (ui/dynamic _ [mode (:mode @*state)]
          (case mode
            :overview (overview/overview slides)
            :present  slide))))))

(redraw)

(defn -main [& args]
  (reset! *window 
    (ui/start-app!
      {:title    "Humble Deck"
       :mac-icon "resources/icon.icns"
       :bg-color 0xFFFFFFFF}
      #'app))
  (set! (.-_colorSpace ^LayerMetalSkija (.getLayer ^Window @*window)) (ColorSpace/getDisplayP3))
  ; (reset! debug/*enabled? true)
  (redraw))

(comment
  (redraw)
  (window/window-rect @*window))