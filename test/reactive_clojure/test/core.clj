(ns reactive-clojure.test.core
  (:use [reactive-clojure.core]
  	    [reactive-clojure.util])
  (:use [clojure.test])
  (:import [java.lang.Thread]))

(deftest atom-tests
	(let [a (atom 5)]
		 (push-change a 6)
		 (is (= @a 6))))

(deftest ratom-tests
	(let [a (atom 5)
		  r (ratom a 4)]
		  (is (= @r 4))
		  (push-change a 7)
		  (is (= @r 7))))

(deftest filter-tests
	(let [t (atom 0)
		  fil (rfilter t #(= % 1))
		  a (ratom fil 5)]
		  (is (= @a 5))
		  (push-change t 7)
		  (is (= @a 5))
		  (push-change t 1)
		  (is (= @a 1))))

(deftest map-tests
	(let [m (rmap nil inc)
		  r (ratom m nil)]
		  (push-change m 5)
		  (is (= @r 6))
		  (push-change m 7)
		  (is (= @r 8))))

(deftest rskip-tests
	(let [m (rskip nil 2)
		  r (ratom m nil)]
		  (is (= @r nil))
		  (push-change m 7)
		  (is (= @r nil))
		  (push-change m 7)
		  (is (= @r nil))
		  (push-change m 7)
		  (is (= @r 7))))

;(deftest pulse-tests
;	(let [a (atom 5)
;		  pulse (rpulse 10)]
;		  (add-subscriber pulse a)
;		  (Thread/sleep 1000)
;		  (is (= @a nil))))

