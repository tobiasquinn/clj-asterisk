(ns clj-asterisk.internal.events
  (:require [clj-asterisk.internal.connection :as connection]
            [clj-asterisk.internal.protocol :as protocol]
            [clj-asterisk.events :as public-events]
            [clojure.tools.logging :as log])
  (:use [slingshot.slingshot :only [throw+ try+]]
        [clj-asterisk.internal.core :only
         [send-message read-response get-connection with-connection* current-context get-action-promise]]))

(defn- start-thread
  "Starts a new thread with the specified function"
  [fn]
  (doto (Thread. fn) (.start)))

(defn- next-line
  "Returns the next line read from the current connection"
  []
  (connection/readline (get-connection)))

(defn- connected?
  []
  (connection/connected? (get-connection)))
  
(defn- queue-response
  "Queues the packet as a response, it's assumed there's a corresponding
   promise waiting for the response inside the context packet list."
  [packet]
  (if-let [p (get-action-promise (read-string (or (:ActionID packet) "0")))]
    (deliver p packet)
    (throw+ {:type ::nopromise :packet packet})))

(defn- dispatch-message
  "Dispatches a message according to it's base type"
  [lines]
  (try+
   (when-let [packet (protocol/ast->clj lines)]
    (cond
     (:Response packet) (queue-response packet)
     (:Event packet) (public-events/handle-event packet (current-context))
     :else (throw+ {:type ::unknown-packet :packet packet})))
   (catch :type e
     (log/error (pr-str e)))
   (catch Object _
     (log/error (:throwable &throw-context) "unexpected error"))))

(defn async-reader
  "Creates a new async reader for packets on the connection"
  [context]
  (start-thread
   (fn []
     (log/info "Starting async reader")
     (with-connection* context
       (loop [line (next-line) acc []]
         (when (and (connected?) line)
           (if (protocol/end-of-message? line)
             (do
               (dispatch-message acc)
               (recur (next-line) []))
             (recur (next-line) (conj acc line)))))))))
