(ns shootout.aleph
  (:require [aleph.http   :as ahttp]
            [shootout.logging   :as log])
  (:use [lamina.core]
        [net.cgrand.moustache]))

(defonce *server* (atom nil))


(defn async-chicken [channel request]
  (enqueue channel
           {:status 200
            :headers {"content-type" "text/plain"}
            :body  "Chickens are sublime creatures."}))

(defn sync-tuna [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Chickens are little dinosaurs."})


(def handlers
     (app
      ["chicken"] { :get (ahttp/wrap-aleph-handler async-chicken) }
      ["tuna"]    { :get sync-tuna }))

(defn start-server []
  (reset! *server*
          (ahttp/start-http-server
           (ahttp/wrap-ring-handler handlers)
           {:port 8080})))

(comment
  (reset! *server* (start-server))

  (@*server*)


  )

