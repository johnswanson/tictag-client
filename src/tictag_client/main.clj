(ns tictag-client.main
  (:gen-class)
  (:require [tictag-client.client :as client]
            [tictag-client.config :refer [config]]
            [reloaded.repl]
            [clojure.core.async :as a :refer [<! go-loop]]
            [tictag-client.utils :as utils]))

(defn do-not-exit! []
  (a/<!!
   (go-loop []
     (let [_ (<! (a/timeout 100000))]
       (recur)))))

(def system (client/system config))

(defn -main [& _]
  (reloaded.repl/set-init! (constantly (utils/system-map system)))
  (reloaded.repl/go)
  (do-not-exit!))

