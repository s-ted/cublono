;   Copyright (c)  Sylvain Tedoldi. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;

(ns cublono.interpreter
  (:require
    [cublono.normalize :as normalize]
    [cublono.util :as util]
    [clojure.string :as str]))


(defprotocol Interpretable
  (interpret [this] "Interpret a Clojure data structure as a hiccup-like structure"))


(defn flatten-lists [coll]
  (reduce #(if (seq? %2)
             (concat %1 %2)
             (conj %1 %2))
          []
          coll))

(defn element
  "Render an element vector as a HTML element."
  [element]
  (let [[type attrs content] (normalize/element element)

        classes (set (:class attrs))]
    (into [] (flatten-lists
               (concat [type (if (empty? classes)
                               attrs
                               (assoc attrs :class (str/join " " classes)))]
                       (map interpret content))))))

(defn- interpret-seq [s]
  (map interpret s))

(extend-protocol Interpretable
  PersistentVector
  (interpret [this]
    (if (util/element? this)
      (element this)
      (interpret-seq this)))

  LazySeq
  (interpret [this]
    (interpret-seq this))

  default
  (interpret [this]
    this))




(defn attributes [{:keys [class] :as attrs}]
  (let [class-set (set
                    (cond
                      (string? class) (str/split class #" ")
                      (coll? class)   (mapcat #(str/split % #" ")
                                              class)))
        sanitized (str/join " " class-set)]
    (-> attrs
        (dissoc :class)
        (#(if-not (str/blank? sanitized)
            (assoc % :class sanitized)
            %)))))
