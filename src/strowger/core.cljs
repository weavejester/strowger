(ns strowger.core
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [goog.object :as obj]
            [goog.events :as events])
  (:import goog.events.KeyCodes))

(defn- convert-key [k]
  (-> k str/lower-case (str/replace "_" "-") keyword))

(def keyword->keycode
  (persistent!
   (reduce
    (fn [m k]
      (let [v (aget KeyCodes k)]
        (if (fn? v) m (assoc! m (convert-key k) v))))
    (transient {})
    (array-seq (obj/getKeys KeyCodes)))))

(def keycode->keyword
  (set/map-invert keyword->keycode))

(def keyword->button
  {:left 0, :middle 1, :right 2})

(def button->keyword
  (set/map-invert keyword->button))

(defn event-key [event]
  (keycode->keyword (.-keyCode event)))

(defn event-button [event]
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

(defn remove-listeners [element key]
  (doto element
    (remove-dom-listeners (-> element listener-map (get key)))
    (update-listener-map! dissoc key)))

(defn add-listeners [element key listeners]
  (doto element
    (remove-listeners key)
    (update-listener-map! assoc key listeners)
    (add-dom-listeners listeners)))
