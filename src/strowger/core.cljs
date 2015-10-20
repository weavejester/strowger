(ns strowger.core
  "Functions for managing DOM events."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [goog.object :as obj]
            [goog.events :as events])
  (:import goog.events.KeyCodes))

(defn- convert-key [k]
  (-> k str/lower-case (str/replace "_" "-") keyword))

(def keyword->keycode
  "A map of the names of keyboard keys to their corresponding codes."
  (persistent!
   (reduce
    (fn [m k]
      (let [v (aget KeyCodes k)]
        (if (fn? v) m (assoc! m (convert-key k) v))))
    (transient {})
    (array-seq (obj/getKeys KeyCodes)))))

(def keycode->keyword
  "A map of key codes to their corresponding keyboard key names."
  (set/map-invert keyword->keycode))

(def keyword->button
  "A map of the names of mouse buttons to their corresponding codes."
  {:left 0, :middle 1, :right 2})

(def button->keyword
  "A map of mouse button codes to their corresponding button names."
  (set/map-invert keyword->button))

(defn event-key
  "Return the name of the key pressed on a `:key-up` or `:key-down` event."
  [event]
  (keycode->keyword (.-keyCode event)))

(defn event-button
  "Return the name of the button clicked on a `:click` or `:dblclick` event."
  [event]
  (button->keyword (.-button event)))

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
  "Remove all the listeners on a DOM element associated with the supplied key.
  See [[add-listeners]]."
  [element key]
  (doto element
    (remove-dom-listeners (-> element listener-map (get key)))
    (update-listener-map! dissoc key)))

(defn add-listeners
  "Add a map of event keywords to listener functions to a DOM element. The key
  argument is a unique value used to identify the listeners so that they can be
  removed using [[remove-listeners]]."
  [element key listeners]
  (doto element
    (remove-listeners key)
    (update-listener-map! assoc key listeners)
    (add-dom-listeners listeners)))
