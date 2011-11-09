(ns reactive-clojure.core
    (:use [clojure.core.match :only [match]])
    (:import (java.util.concurrent ConcurrentLinkedQueue Executor))
    (:import (java.util UUID)))


(defrecord NState [state f listeners])

(defn null-fn [state k v]
    state)
 
(defn make-node [f & opts]
    "Creates a new node with f as the processing function. f should be a function
    that takes [state k v]. Where state is the state returned by the last call to
    f, k is the channel key (always default to :default), and v is the value being
    emitted. Each node should look for ::stop which is a special case value signaling
    the end of the parent stream."
    (let [{:keys [state]} (apply hash-map opts)
          ns (NState. state f {})]
         (agent ns)))

(defn connect [from filter-fn to]
    (send from
          #(assoc-in %1 [:listeners %2] %3)
           to
           filter-fn)
    (await from))

(defn emit 
    ([node k v]
        (send node
             (fn [state k v] 
                 ((:f state) state k v))
             k v))
    ([node filter-fn k v]
        (emit node (filter-fn k) v)))

(defn emit-all [state k v]
    "Sents k v to each listener in state"
    (doseq [[node fns] (:listeners state)]
           (emit node fns k v)))
     

(defn map-state [state f]
    "Applies f to the :state index of state and returns the result"
    (assoc state 
           :state 
           (f (:state state))))

(defn set-state [state v]
    "Sets (:state state) to v"
    (assoc state :state v))
        

(defn r-do [f]
    "Simply executes f against every k v pair received by a parent. f should be
    a function taking two values, a channel key and a value."
    (make-node (fn [state k v]
                   (f k v)
                   state)))

(defn r-filter [f]
    "Creates a node that applies f against every value emitted by the parent, 
    and only passes on those for which f returns true"
    (make-node (fn [state k v]
                   (match [k v]
                       [:default _] (if (f v)
                                    (do (emit-all state k v))
                                        state)
                       [:default ::stop] (NState. nil null-fn nil)
                       [_ _] state))))

(defn r-take [cnt]
    "Creates a node that accepts cnt values before stopping the stream via ::stop"
    (make-node (fn [state k v]
                   (match [k v (:state state)]
                       [:default _ (a :when #(< % cnt))]
                            (if (= (dec cnt) (:state state))
                                (do (emit-all state :default v)
                                    (emit-all state :default ::stop)
                                    (NState. :stopped null-fn nil))
                                (do (emit-all state :default v)
                                    (map-state state inc)))
                       [:default _ nil] state
                       [:default ::stop :stopped] state
                       [:default ::stop _]
                                (do (emit-all state :default ::stop)
                                    (NState. :stopped null-fn nil))))
               :state 0))
                       
                        
           

