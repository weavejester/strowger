(ns strowger.event
  "Functions for managing DOM events."
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn- convert-key [k]
  (-> k str/lower-case (str/replace "_" "-") keyword))

;; Generated from goog.events.KeyCodes
(def keyword->keycode
  "A map of the names of keyboard keys to their corresponding codes."
  {:win-key-ff-linux 0
   :mac-enter 3
   :backspace 8
   :tab 9
   :num-center 12
   :enter 13
   :shift 16
   :ctrl 17
   :alt 18
   :pause 19
   :caps-lock 20
   :esc 27
   :space 32
   :page-up 33
   :page-down 34
   :end 35
   :home 36
   :left 37
   :up 38
   :right 39
   :down 40
   :print-screen 44
   :insert 45
   :delete 46
   :zero 48
   :one 49
   :two 50
   :three 51
   :four 52
   :five 53
   :six 54
   :seven 55
   :eight 56
   :nine 57
   :ff-semicolon 59
   :ff-equals 61
   :question-mark 63
   :at-sign 64
   :a 65
   :b 66
   :c 67
   :d 68
   :e 69
   :f 70
   :g 71
   :h 72
   :i 73
   :j 74
   :k 75
   :l 76
   :m 77
   :n 78
   :o 79
   :p 80
   :q 81
   :r 82
   :s 83
   :t 84
   :u 85
   :v 86
   :w 87
   :x 88
   :y 89
   :z 90
   :meta 91
   :mac-wk-cmd-left 91
   :win-key-right 92
   :mac-wk-cmd-right 93
   :context-menu 93
   :num-zero 96
   :num-one 97
   :num-two 98
   :num-three 99
   :num-four 100
   :num-five 101
   :num-six 102
   :num-seven 103
   :num-eight 104
   :num-nine 105
   :num-multiply 106
   :num-plus 107
   :num-minus 109
   :num-period 110
   :num-division 111
   :f1 112
   :f2 113
   :f3 114
   :f4 115
   :f5 116
   :f6 117
   :f7 118
   :f8 119
   :f9 120
   :f10 121
   :f11 122
   :f12 123
   :numlock 144
   :scroll-lock 145
   :first-media-key 166
   :ff-dash 173
   :last-media-key 183
   :semicolon 186
   :equals 187
   :comma 188
   :dash 189
   :period 190
   :slash 191
   :tilde 192
   :apostrophe 192
   :open-square-bracket 219
   :backslash 220
   :close-square-bracket 221
   :single-quote 222
   :win-key 224
   :mac-ff-meta 224
   :win-ime 229
   :vk-noname 252
   :phantom 255})

(def keycode->keyword
  "A map of key codes to their corresponding keyboard key names."
  (set/map-invert keyword->keycode))

(def keyword->button
  "A map of the names of mouse buttons to their corresponding codes."
  {:left 0
   :middle 1
   :right 2})

(def button->keyword
  "A map of mouse button codes to their corresponding button names."
  (set/map-invert keyword->button))

(defn event-key
  "Return the name of the key pressed on a `:keyup`, `:keydown` or `:keypress`
  event."
  [event]
  (keycode->keyword (.-keyCode event)))

(defn event-button
  "Return the name of the button clicked on a `:click` or `:dblclick` event."
  [event]
  (button->keyword (.-button event)))

(defn client-xy
  "Return the client X and Y coordinates of an event as an `[x y]` vector."
  [event]
  [(.-clientX event) (.-clientY event)])

(defn- body-offset-xy [event target]
  [(- (.-clientX event) (.-clientLeft target))
   (- (.-clientY event) (.-clientTop target))])

(defn- element-offset-xy [event target]
  (let [rect (.getBoundingClientRect target)]
    [(- (.-clientX event) (.-left rect) (.-clientLeft target))
     (- (.-clientY event) (.-top rect)  (.-clientTop target))]))

(defn offset-xy
  "Return the offset X and Y coordinate of an event as an `[x y]` vector.
  This function forces adherance to the W3C standard, avoiding cross-browser
  issues with `.-offsetX` and `.-offsetY`."
  [event]
  (when-let [target (.-target event)]
    (if (or (identical? target js/window) (identical? target js/document))
      (client-xy event)
      (if (identical? target js/document.body)
        (body-offset-xy event target)
        (element-offset-xy event target)))))

(defn- listener-map [element]
  (or (.-strowgerListeners element) {}))

(defn- update-listener-map! [element f & args]
  (set! (.-strowgerListeners element) (apply f (listener-map element) args)))

(defn- add-dom-listeners [element listeners]
  (doseq [[type listener] listeners]
    (.addEventListener element (name type) listener)))

(defn- remove-dom-listeners [element listeners]
  (doseq [[type listener] listeners]
    (.removeEventListener element (name type) listener)))

(defn remove-listeners
  "Remove all the listeners on a DOM element associated with the supplied keys.
  See [[add-listeners]]."
  ([element key]
   (doto element
     (remove-dom-listeners (-> element listener-map (get key)))
     (update-listener-map! dissoc key)))
  ([element key & keys]
   (doseq [key (cons key keys)]
     (remove-listeners element key))))

(defn add-listeners
  "Add a map of event keywords to listener functions to a DOM element. The key
  argument is a unique value used to identify the listeners so that they can be
  removed using [[remove-listeners]]. Multiple key and listener pairs may be
  supplied as additional arguments."
  ([element key listeners]
   (doto element
     (remove-listeners key)
     (update-listener-map! assoc key listeners)
     (add-dom-listeners listeners)))
  ([element key listeners & key-listeners]
   (doseq [[key listeners] (cons [key listeners] (partition 2 key-listeners))]
     (add-listeners element key listeners))))

(defn- wrap-keydown-stop-repeat [handler keys-held]
  (fn [event]
    (let [code (.-keyCode event)]
      (when-not (contains? @keys-held code)
        (swap! keys-held conj code)
        (handler event)))))

(defn- wrap-keyup-stop-repeat [handler keys-held]
  (fn [event]
    (let [code (.-keyCode event)]
      (swap! keys-held disj code)
      (handler event))))

(defn stop-keydown-repeat
  "Filter out repeated keydown events for the same key, caused by the keyboard
  repeat rate. Takes a map of listeners (the same as [[add-listeners]]), and
  returns the map with the `:keyup` and `:keydown` listeners altered to avoid
  repetition."
  [listeners]
  (let [keys-held (atom #{})]
    (-> listeners
        (update :keydown (fnil wrap-keydown-stop-repeat (fn [_])) keys-held)
        (update :keyup   (fnil wrap-keyup-stop-repeat   (fn [_])) keys-held))))
