(defproject rubberlike "0.3.1-SNAPSHOT"
  :description "A Clojure library for creating embedded Elasticsearch servers."
  :url "https://github.com/andersfurseth/rubberlike"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]]
  :profiles {:provided {:dependencies [[org.elasticsearch/elasticsearch "2.3.4"]]}
             :dev {:dependencies [[clojurewerkz/elastisch "2.2.2"]]}}
  :lein-release {:deploy-via :clojars})
