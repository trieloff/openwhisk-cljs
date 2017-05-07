(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]))
            
(nodejs/enable-util-print!)

(def -main (fn [args] 
  (println args)
  (str "Clojure" "Script")))

(set! *main-cli-fn* -main)
(set! js/main -main)