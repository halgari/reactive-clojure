(ns reactive-clojure.test.core
  (:use [reactive-clojure.core])
  (:use [clojure.test]))

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
