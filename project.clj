(defproject rubberlike "0.2.2-SNAPSHOT"
  :description "A Clojure library for creating embedded Elasticsearch servers."
  :url "https://github.com/andersfurseth/rubberlike"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:provided {:dependencies [[org.elasticsearch/elasticsearch "2.3.4"]]}}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]]
  :lein-release {:deploy-via :clojars})
