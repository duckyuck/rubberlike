(ns rubberlike.rest-integration-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojurewerkz.elastisch.query :as q]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [rubberlike.core :as sut]))

(def ^:dynamic *conn*)

(defn initialize-conn [f]
  (let [server (sut/create)]
    (try
      (binding [*conn* (esr/connect (str "http://127.0.0.1:" (sut/port server)))]
        (f))
      (finally
        (sut/stop server)))))

(use-fixtures :once initialize-conn)

(deftest simple-index-and-query-test

  (let [document {:attr "value"}]

    (testing "indexing"
      (is (:created (esd/create *conn* "index" "type" document :refresh true))))

    (testing "querying"
      (let [result (esd/search *conn* "index" "type" :query (q/term :attr "value"))]
        (is (= (-> result :hits :hits first :_source)
               document))))))
