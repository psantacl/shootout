(ns shootout.pipelines
  (:require [clojure.contrib.duck-streams :as ds]
            [clojure.contrib.io :as io]
            [clojure.contrib.shell-out :as sh]
            [lamina.core.pipeline :as pl])
  (:use
   [lamina.core]
   [lamina.executors])
  (:import [org.apache.commons.io IOUtils]))



;;;basic pipeline
(run-pipeline 1
              inc
              dec
              (fn [i] (* 100 i))
              println)

(let [pl (run-pipeline 1
                       inc
                       dec
                       (fn [i] (* 100 i)))]
  @pl)

;;;Pipeline with a asychronous first stage. Pauses execution until the read-channel returns
(let [ch (channel)
      our-future (future
                   (println (format "Enqueuing message from thread %s" (Thread/currentThread)))
                   (Thread/sleep 5000)
                   (enqueue ch "chickens "))
      our-pl     (pipeline :executor (current-executor)
                           (fn [value]
                             (println (format "Starting pipeline in thread(%s)"  (Thread/currentThread) ))
                             value)
                           read-channel
                           (fn [value]
                             (println (format "Resuming pipeline in thread(%s)"  (Thread/currentThread) ))
                             value)
                           (fn [value] (.concat value " are the best!"))
                           (fn [value] (.toUpperCase value )))
      our-pl (our-pl ch)]

  (println (format "This thread(%s) is still processing!"
                   (Thread/currentThread)))
  (println @our-pl))


;;;let's do a shootout!!!
;;;once a pipeline has been completed the on-success fn is triggered
(time
 (let [in-ch (channel)
       out-ch (channel)
       repititions   100]
   (dotimes [i repititions]
     (future
       (enqueue in-ch
                (sh/sh "dig" "www.relaynetwork.com")))
     (on-success (run-pipeline in-ch
                               read-channel
                               (fn [value] (.toUpperCase value )))
                 (fn [result]
                   (.println *out* (str "finished a pipeline: " i))
                   (.flush *out*)))
     (println (str "this thread is still processing: " i)))))

(time
 (let [repititions 100]
   (dotimes [i repititions]
     (println "digging " i)
     (sh/sh "dig" "www.relaynetwork.com"))))


;;;The task macro executes its body on a thread pool similarly to future but returns a resultChannel
;;;instead of a sychronous future object
(let [in-ch (channel)
      repititions   100
      start-time    (System/nanoTime)
      results       (atom [])]
  (dotimes [i repititions]
    (on-success (run-pipeline "wwww.relaynetwork.com"
                              (fn [value]
                                (task (sh/sh "dig" value)))
                              (fn [value] (.toUpperCase value )))

                (fn [result]
                  (println (str "finished a pipeline: " i ))
                  (swap! results conj result)
                  (when (= (count @results)
                           repititions)
                    (println "done: "
                             (Float. (/ (-  (System/nanoTime) start-time )
                                        1000000.0))))))
    (println (str "this thread is still processing: " i))))


(comment

  (defmacro defn! [fn-name arg-spec & body]
    `(defn ~fn-name ~arg-spec
       ~@(map
          (fn [arg]
            `(if-not (isa? ~(:tag (meta arg)) (class ~arg))
               (raise "Error: type-mismatch: expected:'%s' to be a '%s', it was a '%s'"
                      '~arg ~(:tag (meta arg)) (class ~arg))))
          arg-spec)
       ~@body))


  (defmacro defn! [fn-name & fn-parts]
    (let [fn-parts (if (vector? (first fn-parts))
                     (list fn-parts)
                     fn-parts)]

      `(defn ~fn-name
         ~@(map (fn [[arg-spec body]]
                  `(~arg-spec
                    ~@(map
                       (fn [arg]
                         `(if-not (isa? ~(:tag (meta arg)) (class ~arg))
                            (println "Error: type-mismatch: expected:'%s' to be a '%s', it was a '%s'"
                                     '~arg ~(:tag (meta arg)) (class ~arg))))
                       arg-spec)
                    ~body))
                fn-parts))))


  (macroexpand-1 '(defn! tester
                      ([#^Integer tuna]
                         (println "in 1"))
                    ([#^String chicken #^Integer tuna]
                       (println "in 2"))))






  (macroexpand-1 '(defn! tester [#^Integer tuna]
                    (println "in 1")))














  )






