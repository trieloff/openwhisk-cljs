(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]))
            
(nodejs/enable-util-print!)

(println "Hello from the Node!")
(def -main (fn [] (str "Clojure" "Script")))

(defn greet [params]
  (identity {:hans true}))

(set! *main-cli-fn* -main)

(set! js/main -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet greet })
