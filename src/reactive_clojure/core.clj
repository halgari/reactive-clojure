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

;(def executor (java.util.concurrent.Executors/newCachedThreadPool))
(def executor (instant-executor))

(defn queue-action [node]
	(.execute executor node))

(defn swap-with-old! [a f]
	(loop [a a]
		(let [old @a
			  new (f old)]
			  (if (not (.compareAndSet a old new))
			  	  (recur a)
			  	  old))))
			  
			    

(defn make-node [f]
	(let [a (atom {})
		  q (atom (clojure.lang.PersistentQueue/EMPTY))]
		(reify 
			INode
			(connect [this signal node]
				(swap! a
					   assoc
					   node
					   signal)
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
							(doseq [[node signal] @a]
								 (node (signal newval)))
							(recur))
						nil))))))
			
