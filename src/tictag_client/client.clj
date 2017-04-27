(ns tictag-client.client
  (:require [tictagapi.core :as tagtime]
            [chime :refer [chime-ch]]
            [clojure.core.async :as a :refer [<! go-loop]]
            [com.stuartsierra.component :as component]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre]
            [me.raynes.conch :refer [with-programs]]
            [clojure.string :as str]
            [clj-time.coerce :as tc]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.local]
            [tictag-client.utils :as utils]
            [tictag-client.sound :as sound]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn get-one-line [prompt tag-list]
  (sound/play! (io/resource "sounds/ping.wav"))
  (with-programs [dmenu]
    (try
      (dmenu "-i"
             "-b"
             "-p" prompt
             "-fn" "Ubuntu Mono-48"
             "-nb" "#f00"
             "-nf" "#0f0"
             "-sb" "#00f"
             "-sf" "#fff"
             {:in (str/join "\n" tag-list)
              :timeout (* 60 1000)})
      (catch Exception e
        nil))))

(defn request-tags [prompt]
  (let [response-str (get-one-line prompt [])]
    (when (seq response-str)
      (-> response-str
          (subs 0 (dec (count response-str)))
          (str/lower-case)
          (str/split #"[ ,]")
          (set)))))

(defn send-tags-to-server [server-url token time tags]
  (let [{status :status :as response}
        @(http/request {:method  :put
                        :timeout 3000
                        :url     (str server-url "/time/" (tc/to-long time))
                        :headers {"Content-Type"  "application/edn"
                                  "Authorization" token}
                        :body    (pr-str {:tags tags})})]
    (if (= status 200)
      (timbre/debugf "Successful PUT to server! %s" response)
      (do
        (sound/play! (io/resource "sounds/error.wav"))
        (timbre/errorf "Error response from server: %s" response)))))

(defrecord ClientChimer [config]
  component/Lifecycle
  (start [component]
    (timbre/debug "Beginning client chimer")
    (timbre/debugf "Fetching config from remote [%s]..." (:remote-url config))
    (let [{:keys [token]}                    config
          {:keys [tagtime-seed tagtime-gap]} (-> (format "%s/config" (:remote-url config))
                                                 (http/get {:as :text :headers {"Authorization" token}})
                                                 deref
                                                 :body
                                                 edn/read-string)
          _                                  (timbre/debugf "Received configuration from remote. Gap: %s, seed: %s" tagtime-gap tagtime-seed)
          tagtime                            (tagtime/tagtime tagtime-gap tagtime-seed)
          chimes                             (chime-ch
                                              (:pings tagtime)
                                              {:ch (a/chan (a/sliding-buffer 1))})]
      (go-loop []
        (when-let [time (<! chimes)]
          (timbre/debug "Pinging client")
          (let [seconds (t/in-seconds (t/interval time (t/now)))]
            ;; if we missed it by >3 minutes, don't pop it up
            (when (< seconds (* 60 3))
              (when-let [tags (request-tags
                               (format "[%s] PING! (%d seconds ago)"
                                       (utils/local-time time) seconds))]
                (send-tags-to-server (:remote-url config) token time tags))))
          (recur)))
      (assoc component :stop #(a/close! chimes))))
  (stop [component]
    (timbre/debug "Stopping client chimer")
    (when-let [stop-fn (:stop component)]
      (stop-fn))
    (dissoc component :stop)))

(defn system [config]
  {:client (->ClientChimer config)})
