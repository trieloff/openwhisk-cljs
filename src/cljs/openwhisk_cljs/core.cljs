(ns openwhisk-cljs.core
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]))
            
(nodejs/enable-util-print!)

(defn question [id]
  {:question id})

(defn post [id]
  {:post id})

(defn error [url]
  {:error (str url " " "is not a valid StackOverflow URL")})

(defn main [params]
  (println params)
  (def id (re-find #"[\d]{4,}" (:url params)))
  (def question? (not (nil? (re-find #"http://stackoverflow.com/questions/" (:url params)))))
  (def post? (not (nil? (re-find #"http://stackoverflow.com/posts/" (:url params)))))
  (cond
    question? (question id)
    post? (post id)
    :else (error (:url params))))

(set! js/main (fn [args] (clj->js (main (js->clj args :keywordize-keys true)))))