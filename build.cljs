(require '[lumo.build.api :as b])

(b/build "src/cljs"
  {:main 'openwhisk-cljs.core
   :output-to "main.js"
   :optimizations :advanced
      :target :nodejs})