;   Copyright (c)  Sylvain Tedoldi. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;

(ns cublono.transposer
  (:require
    [cublono.interpreter :as interpreter]
    [cublono.util :as util]))

(defprotocol Transposable
  (transpose [this library] "Transpose all tags of a hiccup-like structure"))


(defn element
  "Render an element vector as a HTML element."
  [library element]
  (let [[tag attrs & content :as interpreted] (interpreter/interpret element)

        modifier (get library tag)]
    (cond
      (keyword? modifier) (concat [modifier attrs] (map #(transpose % library)
                                                        content))

      (nil? modifier)     interpreted

      (fn? modifier)      (modifier tag attrs (map #(transpose % library)
                                                   content)))))

(defn- transpose-seq [s library]
  (map #(transpose % library)
       s))

(extend-protocol Transposable
  js/String
  (transpose [this library]
    (str this))

  PersistentVector
  (transpose [this library]
    (if (util/element? this)
      (element library this)
      (transpose (seq this) library)))

  LazySeq
  (transpose [this library]
    (if (util/element? this)
      (element library this)
      (transpose-seq this library)))

  Cons
  (transpose [this library]
    (if (util/element? this)
      (element library this)
      (transpose-seq this library)))

  nil
  (transpose [this library]
    nil))
