(ns reactive-clojure.test.core
  (:use [reactive-clojure.core]
  	    [reactive-clojure.util])
  (:use [clojure.test])
  (:import [java.lang.Thread]))

(deftest atom-tests
	(let [a (atom 5)]
		 (push-change a 6)
		 (is (= @a 6))))

(deftest filter-tests
	(let [a (atom 5)
		  fil (rfilter #(= % 1))]
		  (add-subscriber fil a)
		  (is (= @a 5))
		  (push-change fil 7)
		  (is (= @a 5))
		  (push-change fil 1)
		  (is (= @a 1))))

(deftest pulse-tests
	(let [a (atom 5)
		  pulse (rpulse 10)]
		  (add-subscriber pulse a)
		  (Thread/sleep 1000)
		  (is (= @a nil))))
