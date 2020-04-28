# engelberg/data.union-find

A data structure for tracking connected components.

## About union-find

Graph data structures are often used to track connections between individual elements. When the graph has been built, we may want to ask questions about whether two elements are connected to one another by some path within the graph. We may want to know how many connected components there are within the graph, and we may want to list those components.

[Ubergraph](https://github.com/engelberg/ubergraph) is a Clojure graph data structure that can easily do all those things.

However, using Ubergraph, each time you ask whether two elements are connected in the graph, it has to do a search through the graph. When you list the connected components, it needs to do a traversal of the entire graph.

If you need to frequently analyze the connected components as the graph is being built (e.g., you need to know the status of how many connected components there are each time a new connection is made), or if you are going to make repeated queries about connectivity on the finished graph, a general-purpose graph data structure isn't really the right tool for the job. What you really want is a data structure whose sole purpose is to track the connected components that are formed by individual connections between elements.

The term for such a data structure is a `union-find` data structure. For a classic example of a programming problem that benefits from a union-find data structure, see [Project Euler Problem 186](https://projecteuler.net/problem=186).

Traditionally, union-find is implemented as a mutable data structure using a technique called *path compression*. The idea behind path compression is that connected components are stored as a tree, and each time you query the data structure, you flatten the tree a little bit, so the cost of building the information about the connected components is amortized across the queries. There is a Clojure implementation of union-find with path compression [here](https://github.com/jordanlewis/data.union-find).

However, in my experience, it is far more natural in Clojure to implement union-find as a persistent data structure that eschews amortized path compression. I find you don't gain much by amortizing path compression (the most useful queries require fully compressed paths anyway) and it's rather inconvenient to work with a library where queries about the status of connections return a completely revised data structure along with the answer to your query.

So, this is my preferred way to do union-find in Clojure.

## Usage

Add the following line to your leiningen dependencies:

```
[engelberg/data.union-find "1.0.0"]
```

Require the library in your namespace header:

Require ubergraph in your namespace header:

```clojure
(ns example.core
  (:require [engelberg.data.union-find :as uf]))
```

## Example

```clojure
=> (def uf (-> (uf/union-find 1 2 3 4 5 6)
               (uf/connect 1 2)
               (uf/connect 1 3)
               (uf/connect 4 5)))

=> (uf/count-components uf)
3

=> (uf/components uf)
([2 1 3] [5 4] [6])

=> (uf/count-elements uf)
6

=> (uf/elements uf)
(2 1 3 5 4 6)

=> (uf/connected? uf 2 3)
true

=> (uf/connected? uf 2 4)
false

=> (uf/connected? uf 1 9)
nil   ; Returns nil in this case, because it doesn't know anything about 9

=> (uf/contains-element? uf 1)
true

=> (uf/contains-element? uf 9)
false

=> (uf/component uf 1)
[2 1 3]
```

Note that in the `union-find` constructor, passing in the starting elements is optional. If you initialize the data structure with no elements, it will infer the elements as you add connections.

```clojure
=> (def uf (-> (uf/union-find)
               (uf/connect 1 2)
               (uf/connect 1 3)
               (uf/connect 4 5)))

=> (uf/components uf)
([3 2 1] [5 4])
```

So, here we get back a similar result as the earlier example, except the data structure knows nothing about 6, because it was never mentioned among the connections, nor in the constructor.

## Documentation

The union-find constructor:

```clojure
engelberg.data.union-find/union-find
([] [& elements])
  Constructor for the union-find data structure. Any arguments to the constructor will be added to the data structure as separate elements, initially disconnected from one another.
```

The union-find protocol:
```clojure
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
```

## Examples

See the examples directory for a solution to the Project Euler problem.

## License

Copyright © 2020 Mark Engelberg

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
