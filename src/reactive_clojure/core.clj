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

(defn pushfns [a] 
	(map first (vals @a)))

(defn stopfns [a] 
	(map second (vals @a)))


(defn make-publisher [pub pushfn stopfn]
	"Constructs a generic publisher using the specified
	pushfn and stopfn, this node is then subscribed to pub.
	The routines created by this function will be slightly slower
	than the code that would be produced by a less generic method
	(i.e. by hand). But this is often a much more concise method 
	that will create less boilerplate code.
	
	pusfn should be a function with two arguments: the value being
	pushed, and a sequence of subscriber entries in the form of
	{id [pushfn stopfn]}.
	
	stopfn should be a function that takes a single argument: a sequence
	of subscriber entries in the same format as pushfn"
	(let [a (atom {})
		  o (reify
				IPublisher
					(add-subscriber [this k subpushfn substopfn]
						(swap! a assoc k (list subpushfn substopfn))
						this)
					(remove-subscriber [this k]
						(swap! a dissoc k)
						this)
		        IEdgeNode
		            (push-change [this val]
		            	(pushfn val a))
		            (stop [this]
		            	(stopfn a)))]
		  (if (not (nil? pub))
		  	  (add-subscriber
					pub
					o
					#(pushfn % a)
					#(stopfn a)))
		  o))
	
(defn rmap [pub f]
	"Applies f to each value published by pub, this value
	is then in turn published to any subscribers."
	(make-publisher pub
		       #(let [v (f %1)]
		       	     (doseq [s (pushfns %2)] (s v)))
		       #(doseq [s (stopfns %)] (s))))
	
(defn rfilter [pub f]
	"Applies f to each value published by pub, if f returns
	true the value is passed on to all subscribers. If not, 
	the value is dropped."
	(make-publisher pub
		       #(if (f %1)
		       	     (doseq [s (pushfns %2)] (s %1)))
		       #(doseq [s (stopfns %)] (s))))

(defn rskip [pub cnt]
	"Skips the first cnt number of items published by pub
	after this, all items are published."
	(let [a (atom 0)]
		(make-publisher pub
			#(do (if (<= @a cnt)
					(swap! a inc))
				 (if (> @a cnt)
				 	(doseq [s (pushfns %2)] (s %1))))
			#(doseq [s (stopfns %)] (s)))))
		       
(defn rtake [pub cnt]
	"Takes upto cnt number of items published by pub."
	(let [a (atom 0)]
		(make-publisher pub
			#(do (swap! a inc)
				 (if (<= @a cnt)
				 	 (doseq [s (pushfns %2)] (s %1)))
				 (if (= @a cnt)
				 	(do 
				 		(doseq [s (stopfns %2)] (s %1))
				 		(swap! %2 (fn [_] (identity {}))))))
			#(doseq [s %] (s)))))

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


