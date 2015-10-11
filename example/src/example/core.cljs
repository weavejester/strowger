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
  (strowger/add-listeners ::keyboard {:keydown print-key})
  (strowger/add-listeners ::mouse {:click print-button})
  (strowger/add-listeners ::print {:keydown print-event, :click print-event})
  (strowger/remove-listeners ::print))
