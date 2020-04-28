(ns engelberg.data.union-find)

(defprotocol IUnionFind
  "A protocol for declaring connections between elements and querying status of connected components"
  (count-components [uf] "Returns number of connected components")
  (components [uf] "Returns a sequence of the connected components")
  (count-elements [uf] "Returns number of elements")
  (contains-element? [uf k] "Is key k in uf?")
  (elements [uf] "Returns a sequence of the elements")
  (connect [uf key1 key2] "Returns new union-find data structure where key1 and key2 are connected")
  (connected? [uf key1 key2] "Are key1 and key2 in the same connected component? Returns true if connected, false if not, or nil if key is not present")
  (component [uf k] "Returns component containing key k, or nil if k is not present"))

;; The data structure stores a mapping from each element (key) to a canonical representative element in its component
;; It also stores a mapping from each canonical representative to its component
;; Thus, to lookup any element's component, we look up its canonical representative, and then lookup the representative's component.
;; To see if two elements are in the same component, we see if they have the same canonical representative.

;; For performance, we don't bother storing the self-referential link from a canonical representative element to itself in the keys->canonical map. Its presence can be deduced from canonical->components, so it's a waste of memory.

;; The only really clever algorithmic trick here is that when merging two connected components, we must take care to merge the smaller component into the larger one. That's the key to getting good performance.

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
                       (assoc canonical->components key1 [key1])))
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






