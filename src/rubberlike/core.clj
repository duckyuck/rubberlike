(ns rubberlike.core
  (:import [java.nio.file Paths Files FileVisitResult]))

(defn stringify-map
  [m]
  (zipmap (map name (keys m))
          (map str (vals m))))

(defn settings
  [config]
  (-> (org.elasticsearch.common.settings.ImmutableSettings/settingsBuilder)
      (.put (stringify-map config))
      .build))

(defn health-response
  [node]
  (-> node
      .client
      .admin
      .cluster
      (.prepareHealth (into-array String []))
      .setWaitForYellowStatus
      (.setTimeout (org.elasticsearch.common.unit.TimeValue/timeValueSeconds 5))
      .execute
      .actionGet))

(defn create-node
  [config]
  (let [node (-> (org.elasticsearch.node.NodeBuilder/nodeBuilder)
                 (.local true)
                 (.settings (settings config))
                 .node)]
    (if (.isTimedOut (health-response node))
      (throw (IllegalStateException. "Timed out waiting for yellow status from node."))
      node)))

(defn client
  [server]
  (-> server ::node .client))

(defn bound-address
  [server]
  (-> server
      ::node
      .injector
      (.getInstance org.elasticsearch.http.HttpServer)
      .info
      .address
      .boundAddress
      .address))

(defn port
  [server]
  (-> server bound-address .getPort))

(def delete-recursively-visitor
  (proxy [java.nio.file.SimpleFileVisitor] []
    (visitFile [file _]
      (Files/delete file)
      FileVisitResult/CONTINUE)
    (postVisitDirectory [dir e]
      (if e
        (throw e)
        (do
          (Files/delete dir)
          FileVisitResult/CONTINUE)))))

(defn delete-recursively [path]
  (Files/walkFileTree path delete-recursively-visitor))

(defn data-path
  [{path :path.data temp-data-dir? :rubberlike/temp-data-dir? :as config}]
  (cond
   path (Paths/get path (into-array String []))
   temp-data-dir? (Files/createTempDirectory "rubberlike-" (into-array java.nio.file.attribute.FileAttribute []))
   :else (throw (ex-info "You must supply either path.data or set rubberlike/temp-data-dir? to true" config))))

(def default-config
  {:http.enabled true})

(defn create
  ([]
     (create {}))
  ([config]
     (let [config (merge default-config config)
           config (assoc config :path.data (data-path config))]
       (assoc config ::node (create-node config)))))

(defn stop
  [config]
  (.stop (::node config))
  (when (:rubberlike/temp-data-dir? config)
    (delete-recursively (:path.data config)))
  :stopped)
