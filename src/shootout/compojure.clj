(ns shootout.compojure
  (:require
   [compojure.route                                :as route]
   [shootout.logging   :as log])
  (:use
   compojure.core
   ring.adapter.jetty
   [clj-etl-utils.lang-utils :only [raise]]))


(def *server* (atom nil))

(defn chicken [ & args]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Chickens are little dinosaurs."})


(defroutes handlers
  (GET "/chicken"   [] (chicken)))


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