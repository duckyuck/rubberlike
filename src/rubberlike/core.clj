(ns rubberlike.core
  (:import [java.nio.file Paths Files FileVisitResult]))

(defn settings
  [{:keys [port host data-path disable-http?]}]
  (-> (org.elasticsearch.common.settings.ImmutableSettings/settingsBuilder)
      (.put "http.enabled" (str (not disable-http?)))
      (.put "path.data" (str data-path))
      (cond-> host              (.put "http.host" (str host)))
      (cond-> port              (.put "http.port" (str port)))
      (.build)))

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
  (-> server :node .client))

(defn bound-address
  [server]
  (-> server
      :node
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
  [{:keys [data-dir temp-data-dir?] :as config}]
  (cond
   data-dir (Paths/get data-dir (into-array String []))
   temp-data-dir? (Files/createTempDirectory "rubberlike-" (into-array java.nio.file.attribute.FileAttribute []))
   :else (throw (ex-info "You must supply either data-dir or set temp-data-dir? to true" config))))

(def default-config
  {:disable-http? false
   :temp-data-dir? true})

(defn create
  ([]
     (create {}))
  ([config]
     (let [config (merge default-config config)
           config (assoc config :data-path (data-path config))]
       (assoc config :node (create-node config)))))

(defn stop
  [{:keys [node data-path temp-data-dir?]}]
  (.stop node)
  (when temp-data-dir?
    (delete-recursively data-path))
  :stopped)
