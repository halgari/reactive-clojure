(ns reactive-clojure.test.core
  (:use [reactive-clojure.core]
  	    [reactive-clojure.util])
  (:use [clojure.test])
  (:import [java.lang.Thread]))

(deftest filter-test
	(let [f (register (rfilter #(= % 1)))
		  a (rderef)
		  p (register a )]
		  (connect f p)
		  (is (= @a nil))
		  (update-slot f :default 2)
		  (is (= @a nil))
		  (update-slot f :default 1)
		  (is (= @a 1))))

;(deftest atom-tests
;	(let [a (atom 5)]
;		 (push-change a 6)
;		 (is (= @a 6))))
; 
; (deftest ratom-tests
;	 (let [a (atom 5)
;		   r (ratom a 4)]
;		   (is (= @r 4))
;		   (push-change a 7)
;		   (is (= @r 7))))
; 
 ; (deftest filter-tests
	 ; (let [p (atom 0)
	 	   ; fil (rfilter nil #(= % 1))]
	 	   ; (add-subscriber fil :key #(swap! p (fn [_ _] %)) #(identity nil))
		   ; (push-change fil 7)     
		   ; (push-change fil 1)
		   ; (Thread/sleep 100)
		   ; (is (= @p 1))))

 ; (deftest map-tests
	 ; (let [p (promise)
	 	   ; r (rmap nil #(deliver p %1))]
		   ; (push-change r 5)
		   ; (is (= @p 5))
		   ; ))
; 
; (deftest rskip-tests
	; (let [m (rskip nil 2)
		  ; r (ratom m nil)]
		  ; (is (= @r nil))
		  ; (push-change m 7)
		  ; (is (= @r nil))
		  ; (push-change m 7)
		  ; (is (= @r nil))
		  ; (push-change m 7)
		  ; (is (= @r 7))))

;(deftest pulse-tests
;	(let [a (atom 5)
;		  pulse (rpulse 10)]
;		  (add-subscriber pulse a)
;		  (Thread/sleep 1000)
;		  (is (= @a nil))))

