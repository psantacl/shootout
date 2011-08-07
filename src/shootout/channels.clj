(ns shootout.channels
  (:use
   [lamina.core]))


(def *channel* (channel))

;;;Receive callbacks execute in thread responsible for calling enqueue :(
(do
 (receive *channel* (fn [msg]
                      (println
                       (format  "Received %s in thread %s" msg (Thread/currentThread)))))
 (println (format  "enqueuing in thread %s" (Thread/currentThread)))
 (enqueue *channel* 44))


(do
  (receive *channel* (fn [msg]
                      (println
                       (format  "Received %s in thread %s" msg (Thread/currentThread)))))
 (future
   (println (format  "enqueuing in thread %s" (Thread/currentThread)))
   (enqueue *channel* 44)))

;;;map functiosn across streams
(def *channel* (channel 1 2 3 4))
(map* inc *channel*)
(filter* odd? *channel*)


(def *channel* (channel))

;;;Synchronous.  Unfortunately, this blocks a thread in the thread pool until wait-for-message returns
(future
  (let [msg  (wait-for-message *channel*)]
    (println (format "Got a message(%s) in thread(%s)" msg (Thread/currentThread)))))
(enqueue *channel* 69)


;;;Forked channels are copies of the original, but while messages enqueued into the original will appear in the copy, the inverse is not true.
(def *a* (channel 1 2 3 4 5))
(def *b* (fork *a*))

(dotimes [_ 5]
 (receive *a* println))
(dotimes [_ 5]
 (receive *b* println))

(enqueue *a* :chicken)
(receive *b* println)

(enqueue *b* :tuna)
(receive *a* println)
(receive *b* println)

(comment
;;;*out* is an outputstream writer in the threadpool. maps to swank server output
;;;*out* is a printwriter in the swank thread. maps to repl
  (future
    (def *burger* *out*)))

