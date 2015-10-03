(ns strowger.core
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [goog.object :as obj])
  (:import goog.events.KeyCodes))

(defn- convert-key [k]
  (-> k str/lower-case (str/replace "_" "-") keyword))

(def keyword->keycode
  (persistent!
   (reduce
    (fn [m k]
      (let [v (aget goog.events.KeyCodes k)]
        (if (fn? v) m (assoc! m (convert-key k) v))))
    (transient {})
    (array-seq (obj/getKeys goog.events.KeyCodes)))))

(def keycode->keyword
  (set/map-invert keyword->keycode))
