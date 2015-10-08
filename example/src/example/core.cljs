(ns example.core
  (:require [strowger.core :as strowger]))

(enable-console-print!)

(defn print-event [event]
  (js/console.log event))

(defn print-key [event]
  (prn (strowger/event-key event)))

(defn print-button [event]
  (prn (strowger/event-button event)))

(doto js/window
  (strowger/add-listener ::keyboard [:keydown] print-key)
  (strowger/add-listener ::mouse [:click] print-button)
  (strowger/add-listener ::print [:keydown :click] print-event)
  (strowger/remove-listener ::print))
