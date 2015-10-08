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

(defn add-listener [element type listener]
  (.addEventListener element (name type) listener))

(defn remove-listener [element type]
  (.removeEventListener element (name type)))
