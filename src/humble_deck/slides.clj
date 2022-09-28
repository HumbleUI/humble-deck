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
  (vec
    (flatten
      [(templates/svg "title.svg")
       
       (templates/svg "platforms 0.svg")
       (templates/svg "platforms 1.svg")
       (templates/svg "platforms 2.svg")
       (templates/svg "platforms 3.svg")
       
       (delay
         (ui/stack
           @(templates/image "native.jpg")
           @(templates/section "NATIVE")))
       
       (templates/list {:header "Native is" :range [1 4]} "$$$$" "Organizationally Complicated" "Kinda pointless")

       (templates/label "QT")
       (templates/label "QT\nis C++")

       (templates/label "Java UIs")
       (templates/label "Java UIs\nare cursed 👻")
      
       (delay
         (ui/rect (paint/fill 0xFF2C2E3A)
           @(templates/svg "electron.svg")))
       
       (templates/list {:header "Downsides:" :range [2 5]} "Threading model" "JS Performance" "DOM Performance" "No low-level control")
       
       (delay
         (ui/stack
           @(templates/image "electron_apps.jpg")
           (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
             @(templates/label "1.\nPeople WANT apps"))))
       
       (templates/label "2.\nPeople are OK\nwith non-native UI")

       (delay
         (ui/rect (paint/fill 0xFFF3F3F3)
           @(templates/svg "buttons 0.svg")))
       (templates/image "buttons 1.jpg")
       (templates/label "3.\nThere is no good alternative")
       
       (templates/svg "humbleui.svg")
       
       (templates/list {:range [1 8] :header "Humble UI"}
         "UI Framework"
         "For desktop apps"
         "~ Electron"
         "No browser"
         "No DOM"
         "No JS"
         "JVM Clojure only")
       
       (templates/section "DEMO")
       
       demo
       
       (templates/section "Anatomy of UI framework")
       
       (templates/svg "architecture.svg")
       
       (templates/svg "skia.svg")
       
       (templates/list {:header "Skia" :range [1 10]}
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
       
       (templates/svg "skija.svg")
       
       (templates/list {:header "Skija"}
         "Skia bindings for Java")
       
       (templates/svg "jwm.svg")
       
       (templates/list {:header "JWM" :range [1 7]}
         "Java Window Management"
         "OS integration"
         "Modern capabilities"
         "Multi-monitor, VSync, Color Profiles, HiDPI"
         "High-quality"
         "Indistinguishable from native")
       
       (templates/svg "architecture.svg")
       
       (templates/section "Killer features")
       
       (templates/list {:header "Simple layout" :range [1 3]}
         "Parent imposes size"
         "Child chooses their own size")
       (templates/list {:header "Simple layout" :range [3 3]}
         "(-draw child size)"
         "(-measure child space)"
         "Child chooses their own size")
       
       (templates/section "Composable components")
       (templates/image "attributes.jpg")
       (templates/svg "buttons 2.svg")
       (templates/code
         "(defn button [opts]
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
                (label (:caption opts))))))))))")
       
       (templates/section "Sane text metrics")
       (templates/svg "capsize 0.svg")
       (templates/svg "capsize 1.svg")
       (templates/svg "capsize 2.svg")
       (templates/svg "capsize 3.svg")
       
       (templates/list {:header "Also" :range [1 7]}
         "Wide Color Gamut"
         "OpenType features"
         "OkLab Gradients"
         "Squircles"
         "Pixel-perfect scaled graphics"
         "...")

       (templates/list {:header "Clojure" :range [1 5]}
         "❤️"
         "High-level"
         "Easy to use"
         "Fast"
         "Fast(er than JS)")
       
       (templates/list {:header "Clojure" :range [5 7]}
         "❤️"
         "High-level"
         "Easy to use"
         "Fast(er than JS)"
         "Threads"
         "REPL")
       
       ;; (ui/rect (paint/fill 0xFF00EEEE)
       (templates/section "REPL + UI\n=\n💪🦸‍♂️🦸‍♀️🤳\nSUPERPOWER")
       
       (templates/list {:header "REPL + UI" :range [2 5]}
         "Instant Feedback"
         "play & experiment"
         "like figwheel/shadow-cljs"
         "but without browser")
       
       (templates/list {:header "Status" :range [1 4]}
         "Active development"
         "Pre-alpha"
         "Everything changes"
         "Everything changes. A lot")
       
       (templates/list {:header "Status" :range [4 4]}
         "Active development"
         "Pre-alpha"
         "Everything changes. A lot")
       
       (templates/list {:header "Missing pieces" :range [1 7]}
         "JWM / OS integration (Java, C++)"
         "State management"
         "Rich text"
         "Viewports"
         "Distribution"
         "Testing and adoption")
       
       (templates/svg "links.svg")
       
       (templates/label "Thank you!")
       
       (templates/label "Questions?")])))

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
            (ui/dynamic _ [current (:current @state/*state)]
              (let [slide (nth slides current)]
                (cond-> slide
                  (delay? slide) deref)))))))))
