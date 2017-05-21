(defproject openwhisk-cljs "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojurescript "1.9.521" :exclusions [org.apache.ant/ant]]
                 [funcool/httpurr "0.6.3"]
                 [hiccups "0.3.0"]
                 [org.clojure/clojure "1.8.0"]]
  :plugins [[lein-cljsbuild "1.1.6"]]
  :clean-targets ^{:protect false} ["target"]
  :cljsbuild {
    :builds [
    {:id "server-prod"
              :source-paths ["src/cljs"]
              :compiler {:main openwhisk-cljs.core
                         :output-to "main.js"
                         :target :nodejs
                         :optimizations :simple ;; notice this!
                         }}]})
