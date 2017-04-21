(ns tictag-client.config
  (:require [environ.core :refer [env]]))

(def config
  {:remote-url (env :tictag-server-url)
   :token      (env :tictag-token)})
