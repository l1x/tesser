(ns tesser.bench-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check [clojure-test :refer :all]
                                [generators :as gen]
                                [properties :as prop]]
            [multiset.core :refer [multiset]]
            [criterium.core :refer [with-progress-reporting quick-bench bench]]
            [tesser.core :as t]
            [tesser.simple :as s]
            [tesser.utils :refer :all]
            [clojure.core.reducers :as r]
            [clojure.set :as set]))

(def n 1000000)

(defn long-ary []
  (->> #(rand-int n)
       repeatedly
       (take n)
       long-array))

(defn long-vec []
  (->> #(rand-int n)
       repeatedly
       (take n)
       vec))

(deftest ^:bench array-simple-reduce-sum
  (prn 'array-simple-reduce-sum)
  (let [a (long-ary)]
    (prn 'reduce)
    (quick-bench (reduce + 0 a))

    (prn 'tesser)
    (quick-bench (s/reduce + 0 a))))

(deftest ^:bench array-map-filter-fold-sum
  (prn 'array-map-filter-fold-sum)
  (let [a (long-ary)]
    (prn 'reducers)
    (quick-bench (->> a
                      (r/map inc)
                      (r/filter even?)
                      (r/fold +)))

    (prn "tesser")
    (quick-bench (->> (t/map inc)
                      (t/filter even?)
                      (t/fold +)
                      (t/tesser (partition-all-fast 1024 a))))))

(deftest ^:bench ^:focus vec-map-filter-fold-sum
  (prn 'vec-map-filter-fold-sum)
  (let [a (long-vec)]
    (prn 'reducers)
    (quick-bench (->> a
                      (r/map inc)
                      (r/filter even?)
                      (r/fold +)))

    (prn "tesser")
    (quick-bench (->> (t/map inc)
                      (t/filter even?)
                      (t/fold +)
                      (t/tesser (partition-all-fast 1024 a))))))

; For profiling
(deftest ^:stress stress
  (let [a (long-vec)]
    (dotimes [i 10000000]
      (s/reduce + 0 a))))
