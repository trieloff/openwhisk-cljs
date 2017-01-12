(ns ^:figwheel-always openwhisk-cljs.core
  (:require [cljs.nodejs :as nodejs]
      [cljs.pprint :as pprint]
      [httpurr.client :as http]
      [httpurr.client.node :refer [client]]
      [promesa.core :as p]
      [cljs.core :as core]))
(nodejs/enable-util-print!)

;;(println "Hello from the Node!")
(def -main (fn [] (str "Clojure" "Script")))

;; {:payload (str "Hello from " "Clojure" "Script")
;;                                                                      :http %
;;                                                                      :message (if (= (:whorocks params) "you") "you. you rock!"
;;                                                                                 (str (:whorocks params) " rocks"))
;;                                                                      :echo params}

(defn greet [params]
  (->> (http/get client "http://www.example.com")
       (p/map :headers))
  ;;(identity {:hans true})
  )

;;(p/promise (fn [resolve reject] (resolve (core/clj->js params))))

(defn jswrap [func]
  (fn [p]
    (def nparams (core/js->clj p :keywordize-keys true))
    (core/clj->js (func nparams))))

(defn jswrapp [func]
  (fn [p]
    (def nparams (core/js->clj p :keywordize-keys true))
    (def result (func nparams))
    ;;there is probably a better way to determine if we are getting a promise as result.
    (if (= "[object Promise]" (type->str result))
      (p/map core/clj->js result)
      (core/clj->js result))))

(def greetjs (jswrapp greet))

(set! *main-cli-fn* -main)

(set! (.-exports js/module) #js {:hello -main
                                 :greet (fn [params]
                                   (greetjs params)) })
