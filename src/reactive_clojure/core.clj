(ns reactive-clojure.core)

(defprotocol ISubscribable
	"Defines that this object can be subscribed to from this point on
	the subscriber will be notifed of changes to this object's state"
	(add-subscriber [this subscriber])
	(remove-subscriber [this subscriber]))

(defprotocol ISubscriber
	(push-change [this data])
	(stop [this]))
	
(defn _add-subscriber [this subscriber]
	(swap! (.subscribers this)
		   conj subscriber)
	this)	

(defn _remove-subscriber [this subscriber]
	(swap! (.subscribers this)
		   disj subscriber)
	this)

(defn _reset-subscribers [this]
	(swap! (.subscribers this)
		  #{})
	this)

(defn _notify-subscribers [this value]
	(doseq [sub @(.subscribers this)]
		   (push-change sub value)))

(defn _close-subscribers [this]
	(doseq [sub @(.subscribers this)]
		   (stop sub)))

(deftype RFilter
	[filterfn subscribers]
	ISubscriber
	(push-change [this value]
		(if (filterfn value)
			(_notify-subscribers this value)))
	(stop [this]
		(_close-subscribers this)))
(extend RFilter 
	ISubscribable
		{:add-subscriber _add-subscriber
		 :remove-subscriber _remove-subscriber})

(deftype PrintSink
	[subscribers]
	ISubscriber
	(push-change [this value]
		(println value))
	(stop [this]))

(defn print-sink [] (PrintSink. (atom #{})))
(defn rfilter [fnc] (RFilter. fnc (atom #{})))

