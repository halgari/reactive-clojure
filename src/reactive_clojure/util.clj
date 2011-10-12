(ns reactive-clojure.util
	(:require [reactive-clojure.core :as core])
	(:import (java.util Timer TimerTask Date))
    (:import (reactive_clojure.core IPublisher)))

(def scheduler (Timer. true))

(defn- new-task []
	(let [a (atom #{})]
		(proxy [java.util.TimerTask
				reactive_clojure.core.IPublisher]
				[]
				(add_subscriber [subscriber]
					(swap! a conj subscriber)
					this)
				(remove-subscriber [this subscriber]
					(swap! a disj subscriber)
					this)
		    (run [] 
		    	(doseq [s @a] 
		    		(core/push-change s nil))))))
		

(defn rpulse [period]
	(let [task (new-task)]
		(.schedule scheduler task (Date.) (long period))
		task))
