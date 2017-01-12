(ns ^:figwheel-always openwhisk-cljs.core
  (:require [cljs.nodejs :as nodejs]
      [cljs.pprint :as pprint]
      [cljs.core :as core]))
(nodejs/enable-util-print!)

;;(println "Hello from the Node!")
(def -main (fn [] (str "Clojure" "Script")))

(defn greet [params]
  (println "Got these params…")
  (println params)
  {:payload (str "Hello from " "Clojure" "Script")
   :message (if (= (:whorocks params) "you") "you. you rock!"
    (str (:whorocks params) " rocks"))
   :echo params})
   
(defn greetjs [params]
  (def cparams (core/js->clj (.parse js/JSON (.stringify js/JSON params)) :keywordize-keys true))
                                   (def nparams (core/js->clj params :keywordize-keys true))
                                   (println cparams)
                                   (println nparams)
                                   (core/clj->js (greet cparams))
  )
(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet (fn [params]
                                   (greetjs params)) })