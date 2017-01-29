(ns user
  (:require [tictag-client.client :as client]
            [tictag-client.config :refer [config]]
            [reloaded.repl :refer [system init start stop go reset]]
            [clojure.test :refer :all]
            [clojure.tools.namespace.repl :refer [refresh]]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(clojure.tools.namespace.repl/set-refresh-dirs "dev" "src" "test")

(defn client-system []
  (client/system config))

(reloaded.repl/set-init! client-system)
