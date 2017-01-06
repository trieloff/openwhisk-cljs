(defproject figwheel4node "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.107"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.9"]]
  :clean-targets ^{:protect false} ["target"]
  :cljsbuild {
    :builds [
    {:id "server-prod"
              :source-paths ["src/cljs"]
              :compiler {:main openwhisk-cljs.core
                         :output-to "target/js/figwheel4node-server.core.js"
                         :target :nodejs
                         :optimizations :simple ;; notice this!
                         }}
    {:id "server-dev"
              :source-paths ["src/cljs"]
              :figwheel true
              :compiler {:main openwhisk-cljs.core
                         :output-to "target/js/figwheel4node_server_with_figwheel.js"
                         :output-dir "target/js"
                         :target :nodejs
                         :optimizations :none
                         :source-map true }}]}
                         :figwheel {})