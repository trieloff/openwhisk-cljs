(ns ^:figwheel-always openwhisk-cljs.core
  (:require [cljs.nodejs :as nodejs]
      [cljs.pprint :as pprint]
      [cljs.core :as core]))
(nodejs/enable-util-print!)

;;(println "Hello from the Node!")
(def -main (fn [] (str "Clojure" "Script")))

(defn greet [params]
  (println params)
  (println (js-keys params))
  (println (core/js->clj params))
  ;;(println (keys (core/js->clj params)))
  {:payload (str "Hello from " "Clojure" "Script")
   :message (str (get params "whorocks" "nobody") " rocks")
   :echo params})
  
(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet (fn [params] (core/clj->js (greet params))) })