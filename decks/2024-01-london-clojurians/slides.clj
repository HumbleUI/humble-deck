(ns slides
  (:require
    [humble-deck.common :as common]
    [humble-deck.templates :as t]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.ui.focusable :as focusable])
  (:import
    [java.lang AutoCloseable]))

(def controls
  (promise))

(def textbox
  (promise))

(def debug
  (promise))

(def slides
  [[(t/svg "title.svg")]
   (t/list "Hi!"
     "I’m Nikita"
     "Clojure since 2011")
   [(t/image {:bg 0xFF000000} "me.jpg")]
   [(t/svg "humbleui.svg")]
   (t/list "What is Humble UI?"
     "Framework"
     "For Clojure/JVM"
     "GUI"
     "Desktop"
     "Cross-platform")
   [(t/image "demo_todomvc.png")]
   [controls]
   
   [(t/section "Part I\nWhy now?")]
   [(t/label "1. People WANT apps")]
   [(delay
      (ui/stack
        @(t/image "electron_apps.jpg")
        (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
          @(t/label "1. People WANT apps"))))]
   [(t/label "2. No such thing as native GUI")]
   [(t/label "How is context menu\nSUPPOSED\nto look on Windows?")]
   [(t/image "web_buttons.webp")]
   [(t/image "windows_menus.jpg")]
   [(t/image {:bg 0xFFCCCCCC} "macos_dropdowns.png")]
   
   [(t/label "3. Native is expensive")
    (t/label "3. Native is expensive\nAnd inconvenient")]
   [(t/label "Different teams")
    (t/label "Different teams,\ndifferent stack")
    (t/label "Different teams,\ndifferent stack,\ndifferent languages...")]
   
   [(t/label "4. No good alternatives")]
   
   [(t/label "Quick, you need a cross-platform app!")
    (t/label "Quick, you need a cross-platform app!\nWhad to you use?")]
   
   (t/list "Electron"
     "Performance 😕"
     "Bundle size ☹️"
     "Piles of legacy 😑"
     "Hugely successful 📈")
   
   (t/list "Electron done right"
     ["V8" "V8 → JVM"]
     ["JavaScript" "JavaScript → Clojure"]
     ["CSS/DOM" "CSS/DOM → Custom"])
   
   [(t/section "Part II\nDEMO")]
   
   [(t/section "Part III\nHow does it work?")]
   
   [(t/svg "architecture.svg")]
   [(t/svg "arch_skia.svg")]
   [(t/svg "arch_skija.svg")]
   [(t/svg "arch_jwm.svg")]
   [(t/svg "arch_clojure.svg")]
   [(t/svg "architecture.svg")]
   
   [(t/section "Part IV\nHow is Humble UI different?")]
   
   [(t/label "High-fidelity components")]
   
   [textbox]
   
   [(t/section "Centering things")]
   
   [(t/svg "capsize 0.svg")
    (t/svg "capsize 1.svg")
    (t/svg "capsize 2.svg")
    (t/svg "capsize 3.svg")]
   
   [(t/image "centering.jpg")]
   
   (t/list "High-fidelity components"
     "Wide Color Gamut"
     "OpenType features"
     "OkLab Gradients"
     "Squircles"
     "Pixel-perfect scaled graphics"
     "...")
   
   [(t/label "Performance-oriented")]
   [debug]
   
   [(t/label "Platform-aware")]
   
   [(t/label "Easy to pick up")]
   
   [(t/label {:halign 0} "(require '[io.github.humbleui.ui :as ui])
              
(ui/defcomp app []
  [ui/center
   [ui/label \"Hello, London!\"]])

(ui/start-app!
  (ui/window #'app))")]
   
   [(t/label "REPL + UI = 💪🦸‍♂️🦸‍♀️💪")]
   
   (t/list "Status"
     "Active development"
     "Pre-alpha"
     ["Everything changes"
      "Everything changes. A lot"])
   
   (t/list "Closest plans"
     "VDOM"
     "Multiline text"
     "Text editors"
     "Component library"
     "Viewports"
     "Packaging")

   (t/list "How you can help"
     "JWM / OS integration (Java, C++)"
     "GraalVM"
     "Testing and adoption"
     "Sponsoring 💰")
   
   (t/list "Huge thanks to"
     "Clojurists Together"
     "Roam Research"
     "JetBrains"
     "Patrons and Github Sponsors")
   
   [(t/svg "links.svg")]
   [(t/svg "questions.svg")]
   [(t/label "Thank you!")]
   ])

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
          (for [cmp (@#'focusable/focused this ctx)]
            (do
              (protocols/-set! cmp :focused false)
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

(deliver controls
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

(def *text2
  (atom {:text "Clojure is a robust, practical, and fast programming language with a set of useful features that together form a simple, coherent, and powerful tool."
         :placeholder "Edit me"}))

(deliver textbox
  (unfocus
    (ui/center
      (ui/with-context {:scale 6}
        (ui/default-theme {}
          (ui/with-scale scale
            (ui/width #(* 0.5 (:width %))
              (ui/text-field *text2))))))))

(deliver debug
  (ui/center
    (ui/with-context {:scale 6}
      (ui/default-theme {}
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
                (ui/label "Cache previews")))))))))

(swap! common/*slider
  assoc :max (dec (count slides)))
