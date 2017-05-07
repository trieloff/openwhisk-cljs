(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]))
            
(nodejs/enable-util-print!)

(defn main [params]
  {:args params})

(set! js/main (fn [args] (clj->js (main args))))