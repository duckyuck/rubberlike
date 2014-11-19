(defproject rubberlike "0.1.1"
  :description "A Clojure library for creating embedded Elasticsearch servers."
  :url "https://github.com/andersfurseth/rubberlike"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:provided {:dependencies [[org.elasticsearch/elasticsearch "1.4.0"]]}}
  :lein-release {:deploy-via :clojars})
