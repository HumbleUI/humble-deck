(ns slides
  (:require
    [humble-deck.common :as common]
    [humble-deck.state :as state]
    [humble-deck.templates :as t]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.ui.focusable :as focusable])
  (:import
    [java.lang AutoCloseable]))

(def slides
  [[(t/svg "title.svg")]
   (t/list "Hi!"
     "I’m Nikita"
     "Clojure since 2011")
   [(t/image {:bg 0xFF000000} "me.jpg")]
   [(t/svg "humbleui.svg")]
   (t/list "What is Humble UI?"
     "Cross-platform"
     "Desktop"
     "GUI"
     "For Clojure")
   [(t/image "demo_todomvc.png")
    (t/image "demo_controls.png")]
   
   [(t/section "Part I\nWhy now?")]
   [(t/label "1. People WANT apps")]
   [(delay
      (ui/stack
        @(t/image "electron_apps.jpg")
        (ui/with-context {:fill-text (paint/fill 0xFFFFFFFF)}
          @(t/label "1. People WANT apps"))))]
   [(t/label "2. No such thing as native GUI")]
   [(t/label "How is context menu\nSUPPOSED\nto look on Windows?")]
   [(t/image "windows_menus.jpg")]
   [(t/image {:bg 0xFFCCCCCC} "macos_dropdowns.png")]
   [(t/image "web_buttons.webp")]
   
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
   
   (t/list "...also"
     ["QT"
      "QT, Flutter"
      "QT, Flutter, Swing"
      "QT, Flutter, Swing, JavaFX"])

   (t/list "How is Humble UI different?"
     "High-fidelity interactions"
     "Performance-oriented"
     "Platform-aware"
     "Easy to pick up"
     "REPL")
   
   (t/list "Electron done right"
     ["V8" "V8 → JVM"]
     ["JavaScript" "JavaScript → Clojure"]
     ["CSS/DOM" "CSS/DOM → Custom"])
   
   [(t/section "Part II\nDEMO")]
   ])