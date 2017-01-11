(ns ^:figwheel-always openwhisk-cljs.core
  (:require [cljs.nodejs :as nodejs]
      [cljs.core :as core]))
(nodejs/enable-util-print!)

;;(println "Hello from the Node!")
(def -main (fn [] (str "Clojure" "Script")))

(defn greet [params]
  {:payload (str "Hello from " "Clojure" "Script")
   :echo params})
  
(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet (fn [params] (core/clj->js (greet params)) ) })