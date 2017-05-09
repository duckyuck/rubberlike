# Rubberlike

Rubberlike is a [Clojure](http://clojure.org/) library for creating embedded [Elasticsearch](http://www.elasticsearch.org/) servers. Pretty useful for testing if I may say so myself.

Latest version is `[rubberlike "0.3.0"]`.

Rubberlike requires Java >= 1.7 and quite possibly Elasticsearch 2.x.

Elasticsearch is not included as a dependency in Rubberlike so you would have
to pull it in yourself, e.g. `[org.elasticsearch/elasticsearch "2.3.4"]`.

## Usage

All functions reside in the `rubberlike.core` namespace.

`create` creates and starts an embedded Elasticsearch server instance. Invoking
`create` with no arguments will create temporary directories for Elasticsearch
(home and storage), as well as bind the server instance to a dynamically allocated port.
The temporary directories will be deleted upon calling `stop`.

Alternatively, `create` can be invoked with a map of Elasticsearch configuration
parameters (i.e. the same parameters you would put in your elasticsearch.yml).
Configuration parameter keys can be either keywords or strings.

```clojure
(create {:node.name "Ruiner"
         "network.host" "10.0.0.1"}
```

`rubberlike` would be more than welcome to create and delete your supplied Elasticsearch data
and home directories if you're so inclined. Just set `:rubberlike/temp-data-dir?` and/or
`:rubberlike/temp-home-dir?` to true in the config. This is perfomed by default if you
do not provide `:path.data` or `:home.dir` when calling `create`.

```clojure
> (create {:path.data "/opt/rubberlike/data-dir"
           :path.home "/opt/rubberlike/data-dir"
           :rubberlike/temp-data-dir? true
           :rubberlike/temp-home-dir? true}
```

The object returned from `create` is to be provided as the sole argument to the following functions provided by `rubberlike`.

* `port` will give you the server port, which will come in handy if the port is not specified in the call to `create` and thus is dynamically allocated.
* `client` will give you an instance of `org.elasticsearch.client.Client` for use with Elasticsearch's native API.
* `stop` stops the server. It will also delete data and home directories if you either didn't supply them yourself,
or have the appropriate configuration parameters set (see above).

## Contributions

* [Magnus Rundberget](https://github.com/rundis) for initial port to Elasticsearch 2.x

Much obliged!

## License

Copyright © 2014-2017 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
