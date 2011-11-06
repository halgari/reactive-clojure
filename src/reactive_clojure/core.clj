(ns reactive-clojure.core
    (:use [clojure.core.match :only [match]])
    (:import (java.util.concurrent ConcurrentLinkedQueue Executor))
    (:import (java.util UUID)))


(defrecord NState [state f listeners])

(defn null-fn [state k v]
    state)
 
(defn make-node [f & opts]
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
    (doseq [[node fns] (:listeners state)]
           (emit node fns k v)))
     

(defn r-do [f]
    (make-node (fn [state k v]
                   (f k v)
                   state)))

(defn r-filter [f]
    (make-node (fn [state k v]
                   (match [k v]
                       [:default _] (if (f v)
                                    (do (emit-all state k v))
                                        state)
                       [:default ::stop] (NState. nil null-fn nil)
                       [_ _] state))))
                       
                        
           

