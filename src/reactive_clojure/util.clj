(ns reactive-clojure.util
	(:require [reactive-clojure.core :as core])
	(:import (java.util Timer TimerTask Date))
    (:import (reactive_clojure.core ISubscribable)))

(def scheduler (Timer. true))

(defn- new-task []
	(let [a (atom #{})]
		(proxy [java.util.TimerTask
				reactive_clojure.core.ISubscribable]
				[]
				(add-subscriber [this subscriber]
					(swap! a conj subscriber))
				(remove-subscriber [this subscriber]
					(swap! a disj subscriber))
		    (run [] 
		    	(doseq [s @a] 
		    		(core/push-change s nil))))))
		

(defn rpulse [period]
	(let [task (new-task)]
		(.schedule scheduler task (Date.) (long period))
		task))
