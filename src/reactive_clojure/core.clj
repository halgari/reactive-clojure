(ns reactive-clojure.core
	(:import (java.util.concurrent ConcurrentLinkedQueue Executor))
	(:import (java.util UUID)))

(defprotocol INode
	(connect [this signal node])
	(push-update [this value])
	(disconnect [this node]))

(defn instant-executor []
	(reify java.util.concurrent.Executor
		(execute [this runnable]
			(.run runnable))))

(def threaded-executor-inst (java.util.concurrent.Executors/newCachedThreadPool))
(def instant-executor-inst (instant-executor))

(def default-executor (atom threaded-executor-inst))

(defn use-instant-executor! []
	(swap! default-executor
		   #(%2)
		   instant-executor-inst))

(defn use-instant-executor! []
	(swap! default-executor
		   #(%2)
		   threaded-executor-inst))

(defn queue-action [node]
	(.execute @default-executor node))

(defn swap-with-old! [a f]
	(loop [a a]
		(let [old @a
			  new (f old)]
			  (if (not (.compareAndSet a old new))
			  	  (recur a)
			  	  old))))
			  

(defn generate-signal-fn [signal]
    (if (map? signal)
        (fn [s]
            (apply hash-map
                (flatten
                    (map #(list (get signal (first %))
                                            (second %))
                         s))))
        signal))

(defn make-node [f]
	(let [a (atom {})
		  q (atom (clojure.lang.PersistentQueue/EMPTY))]
		(reify 
			INode
			(connect [this signal node]
			    (let [newfn (generate-signal-fn signal)]
                     (swap! a
                            assoc
                            node
                            newfn))
				node)
			(disconnect [this node]
				(swap! a
					   dissoc
					   node)
				node)

			clojure.lang.IFn
			(invoke [this value]
				(let [nq (swap! q
								#(.cons %1 %2)
								value)]
					(if (nil? (.peek (.pop nq)))
						(queue-action this)
						nil)))
			java.lang.Runnable
			(run [this]
				(let [v (swap-with-old! q
										#(.pop %))
					  pval (.peek v)]
					(if (not (nil? pval))
						(let [newval (f pval)]
							(if (not (nil? newval))
								(doseq [[node signal] @a]
									 (node (signal newval)))))
						(recur)))))))
						

(defn r-filter [f]
	"Creates a node that takes each incoming signal and 
	filters it through f if f returns true, the signal 
	is emitted on :default	otherwise, the signal is dropped"
	(make-node #(if (f %)
			        {:default %}
			        nil)))

(defn r-map [f]
	"Creates a node that takes each incoming signal, 
	 applies f to it and emits the result on :default"
	(make-node #(identity {:default (f %)})))

