;   Copyright (c)  Sylvain Tedoldi. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;

(ns cublono.interpreter-test
  (:require-macros [cljs.test :refer [is are deftest testing]])
  (:require
    [cublono.interpreter :as i]
    cljs.test))

(deftest test-attributes
  (are [attrs expected] (= expected (i/attributes attrs))
       {}                       {}
       {:class ""}              {}
       {:class "aa"}            {:class "aa"}
       {:class "aa bb"}         {:class "aa bb"}
       {:class "aa bb aa"}      {:class "aa bb"}
       {:class ["aa bb"]}       {:class "aa bb"}
       {:class ["aa bb aa"]}    {:class "aa bb"}
       {:class ["aa" "bb"]}     {:class "aa bb"}
       {:class ["aa bb" "aa"]}  {:class "aa bb"}
       {:class '("aa bb")}      {:class "aa bb"}))


(deftest test-interpret-nil
  (is (nil? (i/interpret nil))))

(deftest test-interpret-anything-else
  (are [item expected] (is (= expected (i/interpret item)))
       :a      , :a
       "a"     , "a"
       {:k :v} , {:k :v}))

(deftest test-interpret-shorthand-div-forms
  (is (= [:div {:id "test" :class "klass1"}]
         (i/interpret [:#test.klass1]))))

(deftest test-interpret-shorthand-div-forms-with-a-child
  (is (= [:div {:id "test" :class "klass1"} "child"]
         (i/interpret [:#test.klass1 "child"]))))

(deftest test-interpret-static-children-as-arguments
  (is (= [:div {}
          [:div {:class "1" :key 1}]
          [:div {:class "2" :key 2}]]
         (i/interpret
           [:div
            [:div {:class "1" :key 1}]
            [:div {:class "2" :key 2}]]))))

(deftest test-interpret-div
  (is (= [:div {}]
         (i/interpret [:div]))))

(deftest test-interpret-div-with-string
  (is (= [:div {} "x"]
         (i/interpret [:div "x"]))))

(deftest test-interpret-div-with-strings
  (is (= [:div {} "A" "B"]
         (i/interpret [:div "A" "B"]))))


(deftest test-interpret-div-with-nested-lazy-seq
  (is (= [:div {} "A" "B" :k "C"]
         (i/interpret [:div (map identity ["A" "B" :k "C"])]))))

(deftest test-interpret-div-with-nested-list
  (is (= [:div {} "A" "B"]
         (i/interpret [:div (list "A" "B")]))))

(deftest test-interpret-div-with-nested-vector
  (is (= [:div {} "A" "B"]
         (i/interpret [:div ["A" "B"]])))
  (is (= [:div {} "A" "B"]
         (i/interpret [:div (vector "A" "B")]))))

(deftest test-class-duplication
  (is (= [:div {:class "a b c"}]
         (i/interpret [:div.a.a.b.b.c {:class "c"}])))  )

(deftest test-class-as-set
  (is (= [:div {:class "a b c"}]
         (i/interpret [:div {:class #{"a" "b" "c"}}]))))

(deftest test-class-as-list
  (is (= [:div {:class "a b c"}]
         (i/interpret [:div {:class (list "a" "b" "c")}]))))

(deftest test-class-as-vector
  (is (= [:div {:class "a b c"}]
         (i/interpret [:div {:class (vector "a" "b" "c")}]))))

(deftest test-issue-80
  (is (= [:div {}
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]
          [:div {:class "foo bar"}]]
         (i/interpret
           [:div
            [:div {:class (list "foo" "bar")}]
            [:div {:class (vector "foo" "bar")}]
            (let []
              [:div {:class (list "foo" "bar")}])
            (let []
              [:div {:class (vector "foo" "bar")}])
            (when true
              [:div {:class (list "foo" "bar")}])
            (when true
              [:div {:class (vector "foo" "bar")}])
            (do
              [:div {:class (list "foo" "bar")}])
            (do
              [:div {:class (vector "foo" "bar")}])]))))

(deftest test-with-strings-and-stuffs
  (is (= [:div {}
          "child"
          [:span {:class "a"} "deep"]]
         (i/interpret [:div {}
                       "child"
                       [:span.a {} "deep"]]))))

(deftest test-issue-90
  (is (= [:div {} "a"]
         (i/interpret [:div nil (case :a :a "a")]))))

(deftest test-issue-57
  (let [payload {:username "john" :likes 2}]
    (is (= [:div {}
            [:div {} "john (2)"]
            [:div {} "!Pixel Scout"]]
           (i/interpret
             (let [{:keys [username likes]} payload]
               [:div
                [:div (str username " (" likes ")")]
                [:div "!Pixel Scout"]]))))))



(deftest test-mapped-simple-items
  (is (= [:div {} "a" "b" "c"]
         (i/interpret [:div nil "a" (map identity ["b" "c"])]))))

(deftest test-mapped-composed-items
  (is (= [:div {} "a" [:div {:class "test"} "b"] "c"]
         (i/interpret [:div nil "a" (map identity [[:.test "b"] "c"])]))))
