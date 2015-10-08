(ns example.core
  (:require [strowger.core :as strowger]))

(enable-console-print!)

(defn print-key [event]
  (prn (strowger/keycode->keyword (.-keyCode event))))

(strowger/add-listener js/window :keydown print-key)
