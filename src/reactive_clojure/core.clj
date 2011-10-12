(ns reactive-clojure.core)

(defprotocol IPublisher
	"Defines that this object can be subscribed to from this point on
	the subscriber will be notifed of changes to this object's state"
	(add-subscriber [this k pushfn stopfn])
	(remove-subscriber [this k]))

(defprotocol IEdgeNode
	(push-change [this data])
	(stop [this]))
	
(defn rfilter [pub f]
	(let [a (atom {})
		  o (reify
				IPublisher
					(add-subscriber [this k pushfn stopfn]
						(swap! a assoc k (list pushfn stopfn))
						this)
					(remove-subscriber [this k]
						(swap! a dissoc k)
						this))]
		  (add-subscriber
		  	    pub
		  	    o
		  	  	#(if (f %)
		  		      (doseq [s (vals @a)] ((first s) %)))
		  	  	#(doseq [[key, [_ stopfn]] @a] (stopfn key)))
		  o))

;(defn rmap [f & options]
;	(let [a (atom #{})]
;		  (reify
;				IPublisher
;					(add-subscriber [this subscriber]
;						(swap! a conj subscriber)
;						this)
;					(remove-subscriber [this subscriber]
;						(swap! a conj subscriber)
;						this)
;				ISubscriber
;					(push-change [this val]
;						(doseq [s @a] 
;							(push-change s (f val)))))))
;(defn merge [watch


   	   
	
(extend-type clojure.lang.Atom
	IPublisher
	   (add-subscriber [this k pushfn stopfn]
		   (add-watch this
					  k
					  #(pushfn %4)))
	   (remove-subscriber [this k]
		   (remove-watch this k))
	IEdgeNode
		(push-change [this data]
			(swap! this #(identity %2) data))
		(stop [this]
			nil))

(defn ratom [pub initial]
	(let [o (atom initial)]
		 (add-subscriber pub
		 	 o
		 	 #(swap! o (fn [_ n] n) %)
		 	 #(identity nil))
		 o))

;(defn print-sink []
;	(reify
;		ISubscriber
;			(push-change [this value]
;				(println value))
;			(stop [this]
;				nil)))


