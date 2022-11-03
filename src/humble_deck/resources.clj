(ns humble-deck.resources
  (:require
    [io.github.humbleui.typeface :as typeface]
    [io.github.humbleui.ui :as ui]))

(def typeface-regular
  (typeface/make-from-path "resources/CaseMicro-Regular.otf"))

(def typeface-bold
  (typeface/make-from-path "resources/CaseMicro-Bold.otf"))

(def typeface-code
  (typeface/make-from-path "resources/MartianMono-StdRg.otf"))

(def icon-prev
  (ui/svg "resources/prev.svg"))

(def icon-next
  (ui/svg "resources/next.svg"))

(def icon-overview
  (ui/svg "resources/overview.svg"))

(def icon-present
  (ui/svg "resources/present.svg"))

(def icon-speaker
  (ui/svg "resources/speaker.svg"))

(def icon-full-screen
  (ui/svg "resources/fullscreen.svg"))

(def icon-windowed
  (ui/svg "resources/windowed.svg"))

(def icon-talk-reset
  (ui/svg "resources/talk-reset.svg"))

(def icon-talk-pause
  (ui/svg "resources/talk-pause.svg"))

(def icon-talk-resume
  (ui/svg "resources/talk-resume.svg"))

(def icon-clock
  (ui/svg "resources/clock.svg"))
