(ns rubberlike.core
  (:import [java.nio.file Files FileVisitResult]))

(defn settings
  [data-dir]
  (-> (org.elasticsearch.common.settings.ImmutableSettings/settingsBuilder)
      (.put "http.enabled" "true")
      (.put "path.data" data-dir)
      (.build)))

(defn create-node
  [data-dir]
  (-> (org.elasticsearch.node.NodeBuilder/nodeBuilder)
      (.local true)
      (.settings (settings data-dir))
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

(defn create-temp-dir
  [prefix]
  (Files/createTempDirectory prefix (into-array java.nio.file.attribute.FileAttribute [])))

(defn create-server
  []
  (let [data-path (create-temp-dir "elasticsearch-embedded-server")]
    {:node (create-node (str data-path))
     :data-path data-path}))

(defn stop-server
  [server]
  (-> server :node .stop)
  (-> server :data-path delete-recursively)
  :stopped)
