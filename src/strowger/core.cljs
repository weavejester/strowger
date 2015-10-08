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

(defn- listener-map [element]
  (or (.-strowgerListeners element) {}))

(defn- update-listener-map [element f & args]
  (set! (.-strowgerListeners element) (apply f (listener-map element) args)))

(defn add-listener [element key types listener]
  (doseq [type types]
    (doto element
      (update-listener-map assoc-in [type key] listener)
      (.addEventListener (name type) listener))))

(defn remove-listener [element key]
  (doseq [[type listeners] (listener-map element)]
    (when-let [listener (listeners key)]
      (.removeEventListener element (name type) listener))))
