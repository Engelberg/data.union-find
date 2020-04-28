(ns engelberg.data.union-find)

(defprotocol IUnionFind
  "A protocol for declaring connections between elements and querying status of connected components"
  (count-components [uf] "Returns number of connected components")
  (components [uf] "Returns a sequence of the connected components")
  (count-elements [uf] "Returns number of elements" )
  (contains-element? [uf k] "Is key k in uf?")
  (elements [uf] "Returns a sequence of the elements")
  (connect [uf key1 key2] "Returns new union-find data structure where key1 and key2 are connected")
  (connected? [uf key1 key2] "Are key1 and key2 in the same connected component? Returns true if connected, false if not, or nil if key is not present")
  (component [uf k] "Returns component containing key k, or nil if k is not present"))

(defrecord UnionFind [keys->canonical canonical->components]
  IUnionFind
  (count-components [uf] (count canonical->components))
  (components [uf] (vals canonical->components))
  (count-elements [uf] (+ (count keys->canonical) (count canonical->components)))
  (elements [uf] (apply concat (vals canonical->components)))
  (contains-element? [uf k] (contains? canonical->components (keys->canonical k k)))
  (connect [uf key1 key2]
    (if (= key1 key2)
      (if (contains-element? uf key1) uf
          (->UnionFind keys->canonical
                       (conj canonical->components [key1 [key1]])))
      ;; (let [key->canonical1 (find keys->canonical key1)
      ;;       key->canonical2 (find keys->canonical key2)]
      ;;   (cond
      ;;     (and (nil? key->canonical1) (nil? key->canonical2))
      ;;     (->UnionFind (assoc keys->canonical key1 key1, key2 key1)
      ;;                  (assoc canonical->components key1 [key1 key2]))
      ;;     (nil? key->canonical1)
      ;;     (let [canonical2 (val key->canonical2)]
      ;;       (->UnionFind (assoc keys->canonical key1 canonical2)
      ;;                    (update canonical->components canonical2 conj key1)))
      ;;     (nil? key->canonical2)
      ;;     (let [canonical1 (val key->canonical1)]
      ;;       (->UnionFind (assoc keys->canonical key2 canonical1)
      ;;                    (update canonical->components canonical1 conj key2)))
      ;;     :else
      (let [canonical1 (keys->canonical key1 key1)
            canonical2 (keys->canonical key2 key2)]
        (if (= canonical1 canonical2) uf   ; already the same
            (let [component1 (canonical->components canonical1 [canonical1])
                  component2 (canonical->components canonical2 [canonical2])
                                        ;swap if necessary so component1 is the smaller component
                  [canonical1 canonical2 component1 component2] (if (<= (count component1) (count component2))
                                                                  [canonical1 canonical2 component1 component2]
                                                                  [canonical2 canonical1 component2 component1])
                  new-keys->canonical (into keys->canonical (for [item component1] [item canonical2]))
                  new-canonical->components (-> canonical->components
                                                (dissoc canonical1)
                                                (assoc canonical2 (into component2 component1)))]
              (->UnionFind new-keys->canonical new-canonical->components))))))
  (connected? [uf key1 key2]
    (let [canonical1 (canonical->components (keys->canonical key1 key1))
          canonical2 (canonical->components (keys->canonical key2 key2))]
      (and canonical1 canonical2 (= canonical1 canonical2))))
  (component [uf k]
    (when-let [kc (find keys->canonical k)]
      (get canonical->components (val kc)))))

(def EMPTY (->UnionFind {} {}))

(defn union-find "Constructor for the union-find data structure. Any arguments to the constructor will be added to the data structure as separate elements, initially disconnected from one another."
  ([] EMPTY)
  ([& elements] (->UnionFind {} (into {} (for [e elements] [e [e]])))))





