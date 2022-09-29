(ns humble-deck.slides
  (:require
    [humble-deck.resources :as resources]
    [humble-deck.state :as state]
    [humble-deck.templates :as templates]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.font :as font]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]))

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
              (ui/button (fn [] (swap! *counter inc) (state/redraw))
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
                      (ui/label value))))))))))))
            
            

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
  [[(templates/svg "title.svg")]
   
   [(templates/svg "platforms 0.svg")
    (templates/svg "platforms 1.svg")
    (templates/svg "platforms 2.svg")
    (templates/svg "platforms 3.svg")]
   
   [(delay
      (ui/stack
        @(templates/image "native.jpg")
        @(templates/section "NATIVE")))]
   
   (templates/list "Native is"
     "$$$$"
     "Organizationally Complicated"
     "Kinda pointless")

   [(templates/label "QT")
    (templates/label "QT\nis C++")]

   [(templates/label "Java UIs")
    (templates/label "Java UIs\nare cursed 👻")]
  
   [(templates/svg {:bg 0xFF2C2E3A} "electron.svg")]
   
   (templates/list "Downsides:"
     "Threading model"
     "JS Performance"
     "DOM Performance"
     "No low-level control")
   
   [(delay
      (ui/stack
        @(templates/image "electron_apps.jpg")
        (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
          @(templates/label "1.\nPeople WANT apps"))))]
   
   [(templates/label "2.\nPeople are OK\nwith non-native UI")]

   [(templates/svg {:bg 0xFFF3F3F3} "buttons 0.svg")
    (templates/image "buttons 1.jpg")]
   
   [(templates/label "3.\nThere is no good alternative")]
   
   [(templates/svg "humbleui.svg")]
   
   (templates/list "Humble UI"
     "UI Framework"
     "For desktop apps"
     "~ Electron"
     "No browser"
     "No DOM"
     "No JS"
     "JVM Clojure only")
   
   [(templates/section "DEMO")]
   
   [demo]
   
   [(templates/section "Anatomy of UI framework")]
   
   [(templates/svg "architecture.svg")]
   
   [(templates/svg "skia.svg")]
   
   (templates/list "Skia"
     "Graphics library"
     "Good enough for Chrome"
     "& Android"
     ["& Flutter"
      "& Flutter, Libre Office"
      "& Flutter, Libre Office, Avalonia, ..."]
     "Fast"
     "Modern"
     "OpenGL"
     ["DirectX 11"
      "DirectX 11, 12"]
     ["Metal"
      "Metal, Vulkan"
      "Metal, Vulkan, WebGPU"])
   
   [debug]
   
   [(templates/svg "skija.svg")]
   
   (templates/list "Skija"
     "Skia bindings for Java")
   
   [(templates/svg "jwm.svg")]
   
   (templates/list "JWM"
     "Java Window Management"
     "OS integration"
     "Modern capabilities"
     ["Multi-monitor"
      "Multi-monitor, VSync"
      "Multi-monitor, VSync, Color Profiles"
      "Multi-monitor, VSync, Color Profiles, HiDPI"]
     "High-quality"
     "Indistinguishable from native")
   
   [(templates/svg "architecture.svg")]
   
   [(templates/section "Killer features")]
   
   (templates/list "Simple layout"
     "Parent imposes size"
     "(-draw child size)"
     "Child chooses their own size"
     "(-measure child space)")
   
   [(templates/section "Composable components")]
   [(templates/image "attributes.jpg")]
   [(templates/svg "buttons 2.svg")]
   
   [(templates/code
     "(defn button [opts])))])])
(hoverable {}
(clickable {:on-click (:on-click opts)}
  (clip-rrect (:radius opts)
    (rect (:fill opts)
      (padding (:padding opts)
        (center
          (label (:caption opts)))))))))")
    (templates/code
      "(defn button [opts]
(hoverable {}
(clickable {:on-click (:on-click opts)}
  (clip-diagonal (:radius opts)
    (rect (:fill opts)
      (padding (:padding opts)
        (center
          (label (:caption opts)))))))))")
    (templates/code
      "(defn button [opts]
(hoverable {}
(clickable {:on-click (:on-click opts)}
  (shadow (:shadow opts)
    (clip-diagonal (:radius opts)
      (rect (:fill opts)
        (padding (:padding opts)
          (center
            (label (:caption opts))))))))))")]
   
   [(templates/section "Sane text metrics")]
   
   [(templates/svg "capsize 0.svg")]
   [(templates/svg "capsize 1.svg")]
   [(templates/svg "capsize 2.svg")]
   [(templates/svg "capsize 3.svg")]
   
   (templates/list "Also"
     "Wide Color Gamut"
     "OpenType features"
     "OkLab Gradients"
     "Squircles"
     "Pixel-perfect scaled graphics"
     "...")

   (templates/list "Clojure"
     "❤️"
     "High-level"
     "Easy to use"
     ["Fast"
      "Fast(er than JS)"]
     "Threads"
     "REPL")
          
   [(templates/section "REPL + UI\n=\n💪🦸‍♂️🦸‍♀️🤳\nSUPERPOWER")]
   
   (templates/list "REPL + UI"
     "Instant Feedback"
     "play & experiment"
     "like figwheel/shadow-cljs"
     "but without browser")
   
   (templates/list "Status"
     "Active development"
     "Pre-alpha"
     ["Everything changes"
      "Everything changes. A lot"])
          
   (templates/list "Missing pieces"
     "JWM / OS integration (Java, C++)"
     "State management"
     "Rich text"
     "Viewports"
     "Distribution"
     "Testing and adoption")
   
   [(templates/svg "links.svg")]
   
   [(templates/label "Thank you!\nQuestions?")]])

(swap! state/*slider
  assoc :max (dec (count slides)))

(def slide
  (ui/with-bounds ::bounds
    (ui/dynamic ctx [scale      (:scale ctx)
                     cap-height (-> ctx ::bounds :height (* scale) (quot 30))]
      (let [font-default (font/make-with-cap-height resources/typeface-regular (* scale 10))
            font-body    (font/make-with-cap-height resources/typeface-regular cap-height)
            font-h1      (font/make-with-cap-height resources/typeface-bold    cap-height)
            font-code    (font/make-with-cap-height resources/typeface-code    cap-height)]
        (ui/with-context
          {:font-default font-default
           :font-body font-body
           :font-h1   font-h1
           :font-ui   font-body
           :font-code font-code
           :leading   (quot cap-height 2)
           :unit      (quot cap-height 10)}
          (ui/rect (paint/fill 0xFFFFFFFF)
            (ui/dynamic _ [{:keys [slide subslide]} @state/*state]
              (let [slide (-> slides (nth slide) (nth subslide))]
                (cond-> slide
                  (instance? clojure.lang.IDeref slide) deref)))))))))
