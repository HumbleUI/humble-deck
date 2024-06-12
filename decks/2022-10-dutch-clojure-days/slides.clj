(ns slides
  (:require
    [humble-deck.common :as common]
    [humble-deck.templates :as templates]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.ui.focusable :as focusable])
  (:import
    [java.lang AutoCloseable]))

(def *counter
  (atom 1))

(def *checkbox
  (atom true))

(def *slider
  (atom {:value 50 :min 0 :max 100}))

(def *text
  (atom {:text ""
         :placeholder "Edit me"}))

(core/deftype+ Unfocus [child ^:mut child-rect]
  protocols/IComponent
  (-measure [this ctx cs]
    (core/measure child ctx cs))
  
  (-draw [this ctx ^IRect rect ^Canvas canvas]
    (set! child-rect rect)
    (core/draw-child child ctx child-rect canvas))
  
  (-event [this ctx event]
    (or
      (core/event-child child ctx event)
      (when (and
              (= :mouse-button (:event event))
              (:pressed? event)
              (core/rect-contains? child-rect (core/ipoint (:x event) (:y event))))
        (apply core/eager-or
          (for [cmp (@#'focusable/focused this)]
            (do
              (protocols/-set! cmp :focused? false)
              true))))))
  
  (-iterate [this ctx cb]
    (or
      (cb this)
      (protocols/-iterate child ctx cb)))
  
  AutoCloseable
  (close [_]
    (core/child-close child)))

(defn unfocus [child]
  (->Unfocus child nil))

(def demo
  (ui/with-scale scale
    (unfocus
      (ui/center
        (ui/width (* scale 80)
          (ui/column            
            (ui/button (fn [] (swap! *counter inc) (common/redraw))
              (ui/label "Click me"))
            (ui/gap 0 (* scale 10))
              
            (ui/halign 0
              (ui/dynamic _ [counter @*counter]
                (ui/label (str "Clicks: " counter))))
            (ui/gap 0 (* scale 10))
            
            (ui/text-field *text)
            (ui/gap 0 (* scale 10))
              
            (ui/halign 0
              (ui/checkbox *checkbox
                (ui/label "Check me")))
            (ui/gap 0 (* scale 10))
              
            (ui/halign 0
              (ui/row
                (ui/valign 0.5
                  (ui/toggle *checkbox))
                (ui/gap (* scale 1) 0)
                (ui/clickable
                  {:on-click (fn [_] (swap! *checkbox not))}
                  (ui/valign 0.5
                    (ui/label "Toggle me")))))
            (ui/gap 0 (* scale 10))
            
            (ui/row
              [:stretch 1
               (ui/slider *slider)]
              (ui/gap (* scale 2) 0)
              (ui/max-width [(ui/label "100")]
                (ui/valign 0.5
                  (ui/dynamic _ [value (:value @*slider)]
                    (ui/label value)))))))))))

(def debug
  (ui/center
    (ui/column
      (ui/clickable
        {:on-click (fn [_] (swap! protocols/*debug? not))}
        (ui/row
          (ui/valign 0.5
            (ui/toggle protocols/*debug?))
          (ui/gap 5 0)
          (ui/valign 0.5
            (ui/label "Debug info"))))
      (ui/gap 0 10)
      (ui/clickable
        {:on-click (fn [_] (swap! common/*image-snapshot? not))}
        (ui/row
          (ui/valign 0.5
            (ui/toggle common/*image-snapshot?))
          (ui/gap 5 0)
          (ui/valign 0.5
            (ui/label "Cache previews")))))))

(def slides
  [[(templates/svg "title.svg")]
   
   [(templates/svg "platforms 0.svg")
    (templates/svg "platforms 1.svg")
    (templates/svg "platforms 2.svg")
    (templates/svg "platforms 3.svg")]
   
   [(templates/section "Native")]
   
   (templates/list "Native is"
     "$$$$"
     "Organizationally Complicated"
     "Kinda pointless"
     "What is native?")

   [(delay
      (ui/stack
        @(templates/image "native.jpg")
        @(templates/section "What is native?")))]

   [(templates/section "Cross-platform")]
   
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

   [(templates/svg {:bg 0xFFF3F3F3} "buttons 0.svg")]
   [(templates/image {:bg 0xFF162D3F} "buttons 1.jpg")]
   
   [(templates/label "3.\nThere is no good alternative")]
   
   [(templates/svg "humbleui.svg")]
   
   (templates/list "Humble UI"
     "UI Framework"
     "For desktop apps"
     "~ Electron"
     "No JS"
     "No DOM"
     "No browser"
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
   
   (templates/list "API"
     "Not immediate mode"
     "Not pure functional"
     ["Good old OOP" "Good old component tree"]
     "Practical"
     ["Dream: indistinguashable from native" "Dream: native-quality"])
   
   [(templates/section "Killer features")]
   
   (templates/list "Simple layout"
     "Parent imposes size"
     "(-draw child size)"
     "Child chooses their own size"
     "(-measure child space)")
   
   [(templates/svg "align 0.svg")
    (templates/svg "align 1.svg")
    (templates/svg "align 2.svg")
    (templates/svg "align 3.svg")
    (templates/svg "align 4.svg")
    (templates/svg "align 5.svg")]
   
   (templates/list "Composable components"
     "Simple reusable pieces"
     "Low AND high-level"
     "Build your own components")

   [(templates/image "attributes.jpg")]
   [(templates/svg "comps 0.svg")]
   [(templates/code
      "
(defn button [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
      (clip-rrect (:radius opts)
        (rect (:fill opts)
          (padding (:padding opts)
            (center
              (label (:caption opts)))))))))")]
   [(templates/svg "comps 1.svg")]
   [(templates/code
      "
(defn button2 [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
−     (clip-rrect (:radius opts)
+     (clip-diagonal (:radius opts)
        (rect (:fill opts)
          (padding (:padding opts)
            (center
              (label (:caption opts)))))))))")]
   [(templates/svg "comps 2.svg")]
   [(templates/code
      "
(defn button3 [opts]
  (hoverable {}
    (clickable {:on-click (:on-click opts)}
+     (shadow (:shadow opts)
−       (clip-rrect (:radius opts)
+       (clip-diagonal (:radius opts)
          (rect (:fill opts)
            (padding (:padding opts)
              (center
                (label (:caption opts))))))))))")]
   
   (templates/list "Small reusable parts are"
     "Easier to write"
     "Easier to combine"
     "Have less reasons to change"
     "More likely to fit your problem"
     "Require less defaults undoing")
   
   [(templates/section "Sane text metrics")]
   
   [(templates/svg "capsize 0.svg")]
   [(templates/svg "capsize 1.svg")]
   [(templates/svg "capsize 2.svg")]
   [(templates/svg "capsize 3.svg")]
   [(templates/svg "capsize 4.svg")]
   
   (templates/list "Also"
     "Wide Color Gamut"
     "OpenType features"
     "OkLab Gradients"
     "Squircles"
     "Pixel-perfect scaled graphics"
     "...")

   (templates/list "Clojure-first"
     "❤️"
     "High-level"
     "Easy to use"
     ["Fast"
      "Fast(er than JS)"]
     "Threads"
     "REPL")
          
   [(templates/section "REPL + UI = SUPER🦸‍♂️🦸‍♀️POWER")]
   
   (templates/list "REPL + UI"
     "instant Feedback"
     "play & experiment"
     "like figwheel/shadow-cljs"
     "but without browser")
   
   [(templates/animation "reload.webp")]
   [(templates/image "power.jpg")]
   
   (templates/list "Status"
     "Active development"
     "Pre-alpha"
     ["Everything changes"
      "Everything changes. A lot"])
          
   (templates/list "How you can help"
     "JWM / OS integration (Java, C++)"
     "State management"
     "Rich text"
     "Viewports"
     "Distribution"
     "Testing and adoption")
   
   [(templates/section "One more thing...")]
   [(templates/image {:bg 0xFF8F6D4B} "stickers.jpg")]
   
   [(templates/svg "links.svg")]
   [(templates/svg "questions.svg")]
   [(templates/label "Thank you!")]])

(swap! common/*slider
  assoc :max (dec (count slides)))
