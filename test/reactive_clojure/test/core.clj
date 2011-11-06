(ns reactive-clojure.test.core
  (:use [reactive-clojure.core :as r]
  	    [reactive-clojure.util])
  (:use [clojure.test])
  (:import [java.lang.Thread]))


(deftest filter-test
    (let [a (atom nil)
          d (r/r-do #(reset! a %2))
          f (r/r-filter #(= % 1))]
         (r/connect f identity d)
         (is (= @a nil))
         (r/emit f :default 1)  ; Only pass on matching events
         (await f)
         (is (= @a 1))
         (r/emit f :default 2)
         (is (= @a 1))
         (r/emit f :default :r/stop) ; Don't pass anything after a :stop
         (reset! a nil)
         (r/emit f :default 1)
         (is (= @a nil))))
         

