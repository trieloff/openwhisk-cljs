(defproject openwhisk-cljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojurescript "1.9.521"]]
  :plugins [[lein-cljsbuild "1.1.5"]]
  :clean-targets ^{:protect false} ["target"]
  :cljsbuild {
    :builds [
    {:id "server-prod"
              :source-paths ["src/cljs"]
              :compiler {:main openwhisk-cljs.core
                         :output-to "target/js/openwhisk_cljs.core.js"
                         :target :nodejs
                         :optimizations :simple ;; notice this!
                         }}]})
