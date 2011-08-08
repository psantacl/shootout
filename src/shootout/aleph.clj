(ns shootout.aleph
  (:require [aleph.http   :as ahttp]
            [shootout.logging   :as log]
            [clojureql.core   :as ql])
  (:use [lamina.core]
        [net.cgrand.moustache]))


(defonce *server* (atom nil))

(def *db* {:classname "org.postgresql.PGConnection"
           :subprotocol "postgresql"
           :subname     "//localhost:5432/feedback_development"
           :user        "rails"
           :password    "postgres1"
           :auto-commit true
           :fetch-size  500 })

(def *users* (ql/table *db* :users))

(defn async-chicken [channel request]
  (let [results (deref
                 (-> *users*
                     (ql/sort [:id#desc])))]
    (enqueue channel
             {:status 200
              :headers {"content-type" "text/plain"}
              :body   (:company_name (first results))})))

(defn async-tuna [channel request]
  (Thread/sleep 500)
  (enqueue channel
           {:status 200
            :headers {"content-type" "text/plain"}
            :body    "tuna"}))



(defn sync-tuna [request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "Chickens are little dinosaurs."})


(def handlers
     (app
      ["chicken"] { :get (ahttp/wrap-aleph-handler async-chicken) }
      ["tuna"]    { :get  (ahttp/wrap-aleph-handler async-tuna) }))

(defn start-server []
  (reset! *server*
          (ahttp/start-http-server
           (ahttp/wrap-ring-handler handlers)
           {:port 8080})))


(comment
  (reset! *server* (start-server))

  (@*server*)

  (def *users* (ql/table *db* :users))
  (time @*users*)
  (ql/open-global :postgresql *db*)

  (def *email_templates* (ql/table :postgresql :email_templates))
  (time @*email_templates*)

  (ql/select *users* (ql/where "email like '%1'" "%relay%"))
  (deref (ql/select *users* (ql/where "id=1")))

  (deref (ql/select *users* (ql/where (= :id 1))))



  (let [result  (deref
                 (-> *users*
                     (ql/sort [:id#desc])
                     (ql/take 1)))
        ]
    (def *chicken* result)
    #_(clojure.contrib.json/json-str result))

  (require 'clojure.contrib.json)

  (dissoc (first *chicken*) :created_at :updated_at :confirmed_at :confirmation_sent_at )


  )

