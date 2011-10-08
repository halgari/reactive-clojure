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

(def _std-imp {:add-subscriber _add-subscriber
		 		    :remove-subscriber _remove-subscriber})

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
	_std-imp)

(deftype RMap
	[f subscribers]
	ISubscriber
	(push-change [this value]
		(_notify-subscribers (f value)))
	(stop [this]
		(_close-subscribers this)))
(extend RMap
	ISubscribable
	_std-imp)

(extend-type clojure.lang.Atom
	ISubscribable
	(add-subscriber [this subscriber]
		(add-watch subscriber
			       #(push-change subscriber %4)))
	(remove-subscriber [this] nil)
	ISubscriber
	(push-change [this data]
		(swap! this
			   (fn [_] data)))
	(stop [this] nil))

(deftype PrintSink
	[subscribers]
	ISubscriber
	(push-change [this value]
		(println value))
	(stop [this] nil))

(defn print-sink [] (PrintSink. (atom #{})))
(defn rfilter [fnc] (RFilter. fnc (atom #{})))

