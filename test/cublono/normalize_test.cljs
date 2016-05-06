;   Copyright (c)  Sylvain Tedoldi. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;

(ns cublono.normalize-test
  (:require-macros [cljs.test :refer [is are deftest testing]])
  (:require
    [cublono.normalize :as normalize]
    cljs.test))

(deftest test-compact-map
  (are [x expected]
      (= expected (normalize/compact-map x))
    nil        , nil
    {}         , {}
    {:x nil}   , {}
    {:x []}    , {}
    {:x ["x"]} , {:x ["x"]}))

(deftest test-merge-with-class
  (are [maps expected] (= expected (apply normalize/merge-with-class maps))
    []
    nil

    [{:a 1} {:b 2}]
    {:a 1 :b 2}

    [{:a 1 :class :a} {:b 2 :class "b"} {:c 3 :class ["c"]}]
    {:a 1 :b 2 :c 3 :class ["a" "b" "c"]}

    [{:a 1 :class :a} {:b 2 :class "b"} {:c 3 :class (seq ["c"])}]
    {:a 1 :b 2 :c 3 :class ["a" "b" "c"]}

    ['{:a 1 :class ["a"]} '{:b 2 :class [(if true "b")]}]
    '{:a 1 :class ["a" (if true "b")] :b 2}))

(deftest test-strip-css
  (are [x expected]
      (= expected (normalize/strip-css x))
    nil    , nil
    ""     , ""
    "foo"  , "foo"
    "#foo" , "foo"
    ".foo" , "foo"))

(deftest test-match-tag
  (are [tag expected] (= expected (normalize/match-tag tag))
    :div             , [:div nil []]
    :div#foo         , [:div "foo" []]
    :div#foo.bar     , [:div "foo" ["bar"]]
    :div.bar#foo     , [:div "foo" ["bar"]]
    :div#foo.bar.baz , [:div "foo" ["bar" "baz"]]
    :div.bar.baz#foo , [:div "foo" ["bar" "baz"]]
    :div.bar#foo.baz , [:div "foo" ["bar" "baz"]]))

(deftest test-keep-keyword-namespace-in-tag
  (are [tag expected] (= expected (normalize/match-tag tag))
    :namespace/div     , [:namespace/div nil []]
    :namespace.sub/div , [:namespace.sub/div nil []]))

(deftest test-normalize-class
  (are [class expected] (= expected (normalize/class class))
    nil            , nil
    :x             , ["x"]
    "x"            , ["x"]
    ["x"]          , ["x"]
    [:x]           , ["x"]
    '(if true "x") , ['(if true "x")]
    'x             , ['x]
    '("a" "b")     , ["a" "b"]))

(deftest test-attributes
  (are [attrs expected] (= expected (normalize/attributes attrs))
    nil                           , nil
    {}                            , {}
    {:class nil}                  , {:class nil}
    {:class "x"}                  , {:class ["x"]}
    {:class ["x"]}                , {:class ["x"]}
    '{:class ["x" (if true "y")]} , '{:class ["x" (if true "y")]}))

(deftest test-children
  (are [children expected] (= expected (normalize/children children))
    []          , []
    1           , [1]
    "x"         , ["x"]
    ["x"]       , ["x"]
    [["x"]]     , ["x"]
    [["x" "y"]] , ["x" "y"]
    [:div]      , [[:div]]
    [[:div]]    , [[:div]]
    [[[:div]]]  , [[:div]]))

(deftest test-element
  (are [element expected] (= expected (normalize/element element))
    [:div]                  , [:div {} '()]
    [:div {:class nil}]     , [:div {:class nil} '()]
    [:div#foo]              , [:div {:id "foo"} '()]
    [:div.foo]              , [:div {:class ["foo"]} '()]
    [:div.a.b]              , [:div {:class ["a" "b"]} '()]
    [:div.a.b {:class "c"}] , [:div {:class ["a" "b" "c"]} '()]
    [:div.a.b {:class nil}] , [:div {:class ["a" "b"]} '()]))


(deftest test-keep-keyword-namespace
  (are [element expected] (= expected (normalize/element element))
    [:namespace/div]                  , [:namespace/div {} '()]
    [:namespace.sub/div {:class nil}] , [:namespace.sub/div {:class nil} '()]))
