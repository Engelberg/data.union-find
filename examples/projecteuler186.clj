(ns projecteuler186
  (:require [engelberg.data.union-find :as uf]))

(defn S "Lagged fibonacci generator" [k]
  (if (<= 1 k 55)
    (mod (+ 100003 (* -200003 k) (* 300007 k k k)) 1000000)
    (mod (+ (S (- k 24)) (S (- k 55))) 1000000)))

(def S (memoize S))

(defn calls "Returns a lazy sequence of all the calls" []
  (->> (map S (iterate inc 1))
       (partition 2)
       (remove (fn [[caller called]] (= caller called)))))

(defn connections "Returns lazy sequence of union-find data structure's evolution as connections are made" []
  (reductions (partial apply uf/connect) (uf/union-find) (calls)))

(defn solve []
  (count (take-while #(< (count (uf/component % 524287)) (* 0.99 1000000)) (connections))))


 
