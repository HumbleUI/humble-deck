(ns slides
  (:require
    [humble-deck.common :as common]
    [humble-deck.state :as state]
    [humble-deck.templates :as templates]
    [io.github.humbleui.core :as core]
    [io.github.humbleui.debug :as debug]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.protocols :as protocols]
    [io.github.humbleui.ui :as ui]
    [io.github.humbleui.ui.focusable :as focusable])
  (:import
    [java.lang AutoCloseable]))

(def slides
  [[(templates/svg "title.svg")]
   [(templates/section "Hello!!!")]])