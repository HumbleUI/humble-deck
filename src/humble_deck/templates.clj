(ns humble-deck.templates
  (:refer-clojure :exclude [list])
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [humble-deck.scaler :as scaler]
    [humble-deck.state :as state]
    [io.github.humbleui.paint :as paint]
    [io.github.humbleui.ui :as ui]))

(defn label
  ([s] (label {} s))
  ([_opts s]
   (delay
     (ui/center
       (ui/dynamic ctx [{:keys [font-body leading]} ctx]
         (ui/column
           (interpose (ui/gap 0 leading)
             (for [line (str/split s #"\n")]
               (ui/halign 0.5
                 (ui/label {:font font-body} line))))))))))

(defn code
  ([s] (code {} s))
  ([_opts s]
   (delay
     (ui/center
       (ui/dynamic ctx [{:keys [leading font-code]} ctx]           
         (ui/column
           (for [line (remove str/blank? (str/split s #"\n"))
                 :let [color (cond
                               (str/starts-with? line "−") 0xFFF5C3C1
                               (str/starts-with? line "+") 0xFFBAF0C0
                               :else 0xFFFFFFFF)]]
             (ui/rect (paint/fill color)
               (ui/padding (* 0.75 leading)
                 (ui/halign 0
                   (ui/label {:font font-code} line)))))))))))

(defn image
  ([name]
   (image nil name))
  ([opts name]
   (let [{:keys [bg] :or {bg 0xFFFFFFFF}} opts]
     (delay
       (ui/rect (paint/fill bg)
         (scaler/scaler
           (ui/image (io/file "decks" state/deck name))))))))

(defn animation
  ([name]
   (animation nil name))
  ([opts name]
   (let [{:keys [bg] :or {bg 0xFFFFFFFF}} opts]
     (delay
       (ui/rect (paint/fill bg)
         (scaler/scaler
           (ui/animation (io/file "decks" state/deck name))))))))

(defn svg
  ([name]
   (svg nil name))
  ([opts name]
   (let [{:keys [bg] :or {bg 0xFFFFFFFF}} opts]
     (delay
       (ui/rect (paint/fill bg)
         (scaler/scaler
           (ui/svg (io/file "decks" state/deck name))))))))

(defn section [name]
  (delay
    (ui/center
      (ui/dynamic ctx [{:keys [font-h1 leading]} ctx]
        (ui/column
          (interpose (ui/gap 0 leading)
            (for [line (str/split name #"\n")]
              (ui/halign 0.5
                (ui/label {:font font-h1} line)))))))))

(def icon-bullet
  (ui/dynamic ctx [{:keys [unit]} ctx]
    (ui/row
      (ui/valign 0.5
        (ui/rect (paint/fill 0xFF000000)
          (ui/gap (* 2 unit) (* 2 unit)))))))

(defn list [& args]
  (let [[opts header lines] (if (map? (first args))
                              [(first args) (second args) (nnext args)]
                              [{} (first args) (next args)])
        labels (concat
                 [[(ui/dynamic ctx [{:keys [font-h1]} ctx]
                     (ui/label {:font font-h1} header))]]
                 (map
                   (fn [line]
                     (mapv
                       #(ui/dynamic ctx [{:keys [font-body unit]} ctx]
                          (ui/row
                            icon-bullet
                            (ui/gap (* unit 3) 0)
                            (ui/label {:font font-body} %)))
                       (if (sequential? line) line [line])))
                   lines))]
    (vec
      (for [i (range (:from opts 0) (count labels))
            j (range 0 (count (nth labels i)))]
        (delay
          (ui/center
            (ui/max-width (flatten labels)
              (ui/dynamic ctx [{:keys [leading]} ctx]
                (ui/column
                  (interpose (ui/gap 0 leading)
                    (for [label (concat
                                  (map peek (take i labels))
                                  [(-> labels (nth i) (nth j))])]
                      (ui/halign 0
                        label))))))))))))