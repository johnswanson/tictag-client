(defproject tictag-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [jarohen/chime "0.2.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [http-kit "2.2.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [me.raynes/conch "0.8.0"]
                 [clj-time "0.12.2"]
                 [environ "1.1.0"]
                 [reloaded.repl "0.2.3"]
                 [org.clojars.jds02006/tictagapi "0.1.0-SNAPSHOT"]]
  :main ^:skip-aot tictag-client.main
  :repl-options {:init-ns user}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev"]}})
