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

(defn jswrap [func]
  (fn [p]
    (def nparams (core/js->clj p :keywordize-keys true))
    (core/clj->js (func nparams))))

(def greetjs (jswrap greet))

(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet (fn [params]
                                   (greetjs params)) })
