# rubberlike

A [Clojure](http://clojure.org/) library for creating embedded [Elasticsearch](http://www.elasticsearch.org/) Elasticsearch servers.

Latest version is `[rubberlike "0.1.0-SNAPSHOT"]`. Elasticsearch is not included, you must pull that dependency in yourself.

## Usage

```clojure
(require '[rubberlike.core :refer [create-server stop uri]])

(def server (create-server))

;; Port is dynamically allocated. URI can be fetched via:

(uri server)

;; Do your thing, and finish by tearing down the server.

(stop server)
```

Data is being stored in a temporary directory, which will be deleted when `stop` is called on the server instance.

## License

Copyright © 2014 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
