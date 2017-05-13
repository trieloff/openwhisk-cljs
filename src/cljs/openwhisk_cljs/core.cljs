(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]
            [httpurr.client :as http]
            [promesa.core :as p]
            [httpurr.client.node :refer [client]]))
            
(nodejs/enable-util-print!)

(defn gunzip [in]
  (.toString (.gunzipSync (js/require "zlib") in)))

(defn gzjson [zipped]
  (js->clj (.parse js/JSON (gunzip zipped)) :keywordize-keys true))

(defn question [id key]
  "Gets the question specified by `id` as a map."
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/questions/" id "/")
                    {:query-params {:site "stackoverflow"
                                    :key key}})
          (fn [response]
            (p/resolved (first (:items (gzjson (:body response))))))))

(defn answers [id key]
  "Gets the answers for the question specified by `id` as a list."
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/questions/" id "/answers")
                    {:query-params {:site "stackoverflow"
                                    :key key}})
          (fn [response]
            (p/resolved (:items (gzjson (:body response)))))))

(defn full-question [id key]
  "Gets question `id` including all answers"
  (let [q (p/all [(question id key)
                  (answers id key)])]
    (p/then q (fn [[questn answrs]] {:question questn
                                     :answers answrs}))))

(defn post [id]
  {:post id})

(defn error [url]
  {:error (str url " " "is not a valid StackOverflow URL")})

(defn main [params]
  (def id (re-find #"[\d]{4,}" (:url params)))
  (def question? (not (nil? (re-find #"http://stackoverflow.com/questions/" (:url params)))))
  (def post? (not (nil? (re-find #"http://stackoverflow.com/posts/" (:url params)))))
  (cond
    question? (full-question id (:key params))
    post? (post id)
    :else (error (:url params))))

(set! js/main (fn [args] (clj->js (main (js->clj args :keywordize-keys true)))))