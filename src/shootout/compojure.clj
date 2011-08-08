(ns shootout.compojure
  (:require
   [compojure.route                                :as route]
   [shootout.logging   :as log]
   [clojureql.core   :as ql])
  (:use
   compojure.core
   ring.adapter.jetty
   [clj-etl-utils.lang-utils :only [raise]]))


(def *server* (atom nil))

(def *db* {:classname "org.postgresql.PGConnection"
           :subprotocol "postgresql"
           :subname     "//localhost:5432/feedback_development"
           :user        "rails"
           :password    "postgres1"
           :auto-commit true
           :fetch-size  500 })

(def *users* (ql/table *db* :users))


(defn chicken [ & args]
  (let [results (deref
                 (-> *users*
                     (ql/sort [:id#desc])))]
    {:status 200
     :headers {"content-type" "text/plain"}
     :body (:company_name (first results))}))

(defn tuna [ & args]
  (Thread/sleep 5000)
  {:status 200
   :headers {"content-type" "text/plain"}
   :body  "tuna!!!"})


(defroutes handlers
  (GET "/chicken"   [] (chicken))
  (GET "/tuna"   [] (tuna)))


(defn stop-server []
  (if (not @*server*)
    (raise "Error: server not running."))
  (log/info "stopping server")
  (.stop @*server*)
  (log/info "stopped")
  (reset! *server* nil))


(defn start-server []
  (if @*server*
    (raise "Error: server already running, stop it first."))
  (log/info "starting server")
  (reset! *server* (run-jetty handlers {:port 8081 :join? false }))
  (log/info "server started: %s" @*server*))


(comment
  (def *chicken* (run-jetty handlers {:port 8081 :join? false }))

  (start-server)
  (stop-server)
  @*server*
  )