(ns example.core
  (:require [strowger.event :as event]))

(enable-console-print!)

(defn print-event [event]
  (js/console.log event))

(defn print-key [event]
  (prn (event/event-key event)))

(defn print-button [event]
  (prn [(event/event-button event)
        (event/client-xy event)
        (event/offset-xy event)]))

(doto js/window
  (event/add-listeners ::keyboard (event/stop-keydown-repeat {:keydown print-key}))
  (event/add-listeners ::mouse {:click print-button})
  (event/add-listeners ::print-keys   {:keyup print-event, :keydown print-event}
                       ::print-clicks {:click print-event})
  (event/remove-listeners ::print-keys ::print-clicks))
