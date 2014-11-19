(ns rubberlike.core
  (:import [java.nio.file Paths Files FileVisitResult]))

(defn settings
  [{:keys [port data-path disable-http?]}]
  (-> (org.elasticsearch.common.settings.ImmutableSettings/settingsBuilder)
      (.put "http.enabled" (str (not disable-http?)))
      (.put "path.data" (str data-path))
      (cond-> port (.put "http.port" (str port)))
      (.build)))

(defn create-node
  [config]
  (-> (org.elasticsearch.node.NodeBuilder/nodeBuilder)
      (.local true)
      (.settings (settings config))
      .node))

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

(defn host
  [server]
  (-> server bound-address .getHostName))

(defn port
  [server]
  (-> server bound-address .getPort))

(defn uri-host
  [server]
  (if (instance? java.net.Inet6Address (-> server bound-address .getAddress))
    (str "[" (host server) "]")
    (host server)))

(defn uri
  [server]
  (str "http://" (uri-host server) ":" (port server)))

(defn client
  [server]
  (-> server :node .client))

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
