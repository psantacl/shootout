(ns shootout.netty
  (:use
   [clj-etl-utils.lang-utils :only [raise]])
  (:import
   [java.util.concurrent Executors]
   [java.net InetSocketAddress]
   [org.jboss.netty.channel.socket.nio NioServerSocketChannelFactory]
   [org.jboss.netty.bootstrap ServerBootstrap]
   [org.jboss.netty.channel
    ChannelPipelineFactory
    Channels
    SimpleChannelHandler
    ChannelHandler]
   [org.jboss.netty.buffer ChannelBuffers]
   [java.nio.charset Charset]))



(defn make-handler []
  (proxy [SimpleChannelHandler] []
    (messageReceived [ctx e]
                     (let [ch  (.getChannel e)
                           msg-bytes (.getBytes "chicken received!" (Charset/forName "UTF-8"))
                           msg (ChannelBuffers/copiedBuffer msg-bytes)]
                       (.write ch msg)))
    (exceptionCaught [ctx e]
                     (-> (.getChannel e) (.close)))))


(defn start-netty-server []
  (let [ch-factory (NioServerSocketChannelFactory. (Executors/newCachedThreadPool)
                                                   (Executors/newCachedThreadPool))
        bootstrap       (ServerBootstrap. ch-factory)
        our-pipeline    (doto (Channels/pipeline)
                          (.addLast "handler" (make-handler)))
        pl-factory      (reify ChannelPipelineFactory
                               (getPipeline [this]
                                            our-pipeline))]
    (.setPipelineFactory bootstrap pl-factory)
    (.setOption bootstrap "child.tcpNoDelay" true)
    (.setOption bootstrap "child.keepAlive" true)
    (.bind bootstrap (InetSocketAddress. 8080) )))

(comment

  (def *server* (start-netty-server))
  (.close *server*)


  )




