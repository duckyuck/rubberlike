(ns rubberlike.core
  (:import [java.nio.file Paths Files FileVisitResult]))

(defn stringify-map
  [m]
  (zipmap (map name (keys m))
          (map str (vals m))))

(defn settings
  [config]
  (-> (org.elasticsearch.common.settings.Settings/settingsBuilder)
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
      (throw (IllegalStateException. "Timed out waiting for Elasticsearch to start"))
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
      .publishAddress))

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

(defn resolve-path [config dir-attr temp-dir?-attr]
  (let [path (dir-attr config)
        temp-data-dir? (temp-dir?-attr config)]
    (cond
      path (Paths/get path (into-array String []))
      temp-data-dir? (Files/createTempDirectory (str "rubberlike-elasticsearch-" (name dir-attr))
                                                (into-array java.nio.file.attribute.FileAttribute []))
      :else (throw (ex-info (str "You must supply either " dir-attr " or set " temp-dir?-attr " to true") config)))))

(def default-config
  {:http.enabled true})

(defn reify-config [config]
  (merge config
         {:path.data (resolve-path config :path.data :rubberlike/temp-data-dir?)
          :path.home (resolve-path config :path.home :rubberlike/temp-home-dir?)}))

(defn merge-with-defaults [config]
  (-> (merge default-config config)
      (cond-> (and (nil? (:path.data config))
                   (nil? (:rubberlike/temp-data-dir? config)))
        (assoc :rubberlike/temp-data-dir? true))
      (cond-> (and (nil? (:path.home config))
                   (nil? (:rubberlike/temp-home-dir? config)))
        (assoc :rubberlike/temp-home-dir? true))))

(defn create
  ([]
     (create {}))
  ([config]
   (let [config (-> config merge-with-defaults reify-config)]
     (assoc config ::node (create-node config)))))

(defn stop
  [config]
  (.close (::node config))
  (when (:rubberlike/temp-data-dir? config)
    (delete-recursively (:path.data config)))
  (when (:rubberlike/temp-home-dir? config)
    (delete-recursively (:path.home config)))
  :stopped)
