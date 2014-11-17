# Rubberlike

Rubberlike is a [Clojure](http://clojure.org/) library for creating embedded [Elasticsearch](http://www.elasticsearch.org/) servers. Pretty useful for testing if I may say so myself.

Latest version is `[rubberlike "0.1.0"]`.

Rubberlike requires Java >= 1.7 and quite possibly Elasticsearch >= 1.4.x.

Elasticsearch is not included as a dependency in Rubberlike so you would have to pull it in yourself, e.g. `[org.elasticsearch/elasticsearch "1.4.0"]`.

## Usage

All functions reside in the `rubberlike.core` namespace.

`create` creates and starts an embedded Elasticsearch server instance. Invoking `create` with no arguments will create a temporary directory for data storage, as well as bind the server instance to a dynamically allocated port.

Alternatively, `create` can be invoked with a map of configuration parameters. These are:
*  `port` to define which port Elasticsearch will bind to.
*  `data-dir` pointing to the directory where data is to be stored.
*  `temp-data-dir?` which if set to true will tell Rubberlike to create a temporary data directory for you.

The object returned from `create` is to be provided as the sole argument to the following functions.

`uri` will give you the server URI, which will come in handy if the port is being dynamically allocated. There is also `port` and `host` methods to obtain these parts individually.

The server can be stopped by calling `stop`. If `temp-data-dir?` in the call to `create` was set to `true`, the temporary directory will be deleted upon calling `stop`.

Let's summarize this with an example:

```clojure
(require '[rubberlike.core :refer [create stop port]])

;; Create and start a new server.

> (def server (create))
#'user/server

;; Port is dynamically allocated as we didn't specify one. Let's get a hold of it.

> (port server)
9212

;; Assuming we've done our thing with this instance, let's tear it down.

> (stop server)
:stopped
```

## License

Copyright © 2014 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
