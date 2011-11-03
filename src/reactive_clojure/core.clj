(ns reactive-clojure.core
	(:import (java.util.concurrent ConcurrentLinkedQueue Executor))
	(:import (java.util UUID)))


(defrecord NState [state f listeners])

(defrecord NListener [filter-fn stopf-fn])
 
(defn make-node [f & opts]
	(let [{:keys [state]} (apply hash-map opts)
		  ns (NState. state f {})]
		 (agent ns)))

(defn connect [from filter-fn stopf-fn to]
	(send from
		  #(assoc-in %1 [:listeners %2] 
		  	  (NListener. %3 %4))
		   to
		   filter-fn
		   stopf-fn)
	(await from))

(defn emit 
    ([node k v]
        (send node
             (fn [state k v] 
                 ((:f state) state k v))
             k v))
    ([node listener k v]
        (emit node (:filter-fn listener) (:stopf-fn listener) k v))
    ([node filter-fn stopf-fn k v]
        (if (= k :stop)
            (emit node (stopf-fn k) v)
            (emit node (filter-fn k) v))))

(defn emit-all [state k v]
	(doseq [[node fns] (:listeners state)]
		   (emit node fns k v)))
	 

(defn r-do [f]
	(make-node (fn [state k v]
			 	   (f k v)
			 	   state)))

(defn r-filter [f]
	(make-node (fn [state k v]
			   	   (if (f v)
			   	   	   (emit-all state k v))
			   	   state)))
			   	   	   
						
		   

