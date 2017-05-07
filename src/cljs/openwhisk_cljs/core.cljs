(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]
            [httpurr.client :as http]
            [promesa.core :as p]
            [httpurr.client.node :refer [client]]))
            
(nodejs/enable-util-print!)

(defn question [id key]
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/questions/" id "/")
                    ;"http://www.example.com"
                    {:headers {"Accept-Encoding" "identity"}
                     :query-params {:site "stackoverflow"
                                    :key key}})
          (fn [response]
            ;(println (js/require "zlib"))
            (p/resolved (.toString (.gunzipSync (js/require "zlib") (:body response)))))))

(defn post [id]
  {:post id})

(defn error [url]
  {:error (str url " " "is not a valid StackOverflow URL")})

(defn main [params]
  (def id (re-find #"[\d]{4,}" (:url params)))
  (def question? (not (nil? (re-find #"http://stackoverflow.com/questions/" (:url params)))))
  (def post? (not (nil? (re-find #"http://stackoverflow.com/posts/" (:url params)))))
  (cond
    question? (question id (:key params))
    post? (post id)
    :else (error (:url params))))

(set! js/main (fn [args] (clj->js (main (js->clj args :keywordize-keys true)))))