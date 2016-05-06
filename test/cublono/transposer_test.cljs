;   Copyright (c)  Sylvain Tedoldi. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;

(ns cublono.transposer-test
  (:require-macros [cljs.test :refer [is are deftest testing]])
  (:require
    [cublono.transposer :as t]
    cljs.test))

(deftest test-change-tag-name
  (is (= [::div {:class "a b c"}]
         (t/transpose [:div.a.a.b.b.c {:class "c"}]
                      {:div ::div})))

  (is (= [:div {:class "a b c"}]
         (t/transpose [:div.a.a.b.b.c {:class "c"}]
                      {:div1 ::div}))))

(deftest test-change-tag-name-with-children
  (is (= [::div {:class "a b c"} "child" "child2"]
         (t/transpose [:div.a.a.b.b.c {:class "c"} "child" "child2"]
                      {:div ::div})))

  (is (= [:div {:class "a b c"} "child" "child2"]
         (t/transpose [:div.a.a.b.b.c {:class "c"} "child" "child2"]
                      {:div1 ::div}))))

(deftest test-change-tag-name-deep
  (is (= [::div {:class "a b c"}
          "child"
          [::span {} "deep"]]
         (t/transpose [:div.a.a.b.b.c {:class "c"}
                       "child"
                       [:span {} "deep"]]
                      {:div  ::div
                       :span ::span}))))

(deftest test-apply-function
  (is (= [:div {:class "a b c" :some-attr-k :some-attr-v} "new-child"]
         (t/transpose [:div.a.a.b.b.c {:class "c"}]
                      {:div (fn [tag attrs content]
                              [tag
                               (assoc attrs
                                      :some-attr-k :some-attr-v)
                               "new-child"])}))))

(deftest test-apply-function-deep
  (is (= [:div {:class "a b c" :some-attr-k :some-attr-v}
          "new-child"
          [::span {} "deep"]]
         (t/transpose [:div.a.a.b.b.c {:class "c"}
                       [:span {} "deep"]]
                      {:span ::span
                       :div  (fn [tag attrs content]
                               (concat [tag
                                        (assoc attrs
                                               :some-attr-k :some-attr-v)
                                        "new-child"]
                                       content))})))

  (is (= [::div {:class "a b c"}
          [:span {:some-attr-k :some-attr-v} "new-child" "deep"]]
         (t/transpose [:div.a.a.b.b.c {:class "c"}
                       [:span {} "deep"]]
                      {:div  ::div
                       :span (fn [tag attrs content]
                               (concat [tag
                                        (assoc attrs
                                               :some-attr-k :some-attr-v)
                                        "new-child"]
                                       content))}))))

(deftest test-apply-function-with-children
  (is (= [:div {:class "a b c" :some-attr-k :some-attr-v} "new-child1" "new-child2"]
         (t/transpose [:div.a.a.b.b.c {:class "c"} "child1" "child2"]
                      {:div (fn [tag attrs content]
                              (concat [tag (assoc attrs
                                                  :some-attr-k :some-attr-v)]
                                      (map #(str "new-" %) content)))}))))
