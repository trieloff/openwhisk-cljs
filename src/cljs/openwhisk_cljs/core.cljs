(ns openwhisk-cljs.core
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require [cljs.core :as core]
            [cljs.nodejs :as nodejs]
            [httpurr.client :as http]
            [promesa.core :as p]
            [hiccups.runtime :as hiccupsrt]
            [httpurr.client.node :refer [client]]))

(def zlib (js/require "zlib"))
(def defaultfilter "withbody")

(nodejs/enable-util-print!)

(defn gunzip [in len]
  ;(println (.-Z_FINISH (.-constants zlib)))
  (def unzipped (.toString (.gunzipSync zlib in #js {:finishFlush  (.-Z_SYNC_FLUSH (.-constants zlib))})))
  (identity unzipped))

(defn gzjson [zipped len]
  "Turn a GZipped string into JSON"
  (js->clj (.parse js/JSON (gunzip zipped len)) :keywordize-keys true))

(defn question
  "Gets the question specified by `id` as a map."
  [id key]
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/questions/" id "/")
                    {:query-params {:site "stackoverflow"
                                    :key key
                                    :filter defaultfilter}})
          (fn [response]
            (p/resolved (first (:items (gzjson (:body response) (get (-> response :headers) "Content-Length"))))))))

(defn answers
  "Gets the answers for the question specified by `id` as a list."
  [id key]
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/questions/" id "/answers")
                    {:query-params {:site "stackoverflow"
                                    :key key}})
          (fn [response]
            (p/resolved (:items (gzjson (:body response) (get (-> response :headers) "Content-Length")))))))

(defn full-question
  "Gets question `id` including all answers. Optionally, filter answers with
  function `pred`."
  ([id key]
    (full-question id key identity))
  ([id key pred]
   (let [q (p/all [(question id key)
                   (answers id key)])]
     (p/then q (fn [[questn answrs]] {:question questn
                                      :answers  (filter pred answrs)})))))

(defn answer [id key]
  (p/then (http/get client
                    (str "https://api.stackexchange.com/2.2/answers/" id)
                    {:query-params {:site "stackoverflow"
                                    :key key
                                    :filter defaultfilter}})
          (fn [response]
            (p/resolved (:items (gzjson (:body response) (get (-> response :headers) "Content-Length")))))))

(defn full-answer [aid qid key]
  (let [q (p/all [(question qid key)
                  (answer aid key)])]
    (p/then q (fn [[questn answr]] {:question questn
                                     :answers  answr}))))

(defn html-question [{:keys [question answer] {:keys [title]} :question}]
  (html [:div {:class (str "stackoverflow-question" " " (if true "accepted" "open"))}
         [:div {:class "meta"}
          [:span {:class "votes"} "11 votes"]
          [:span {:class (str "answers" " " (if true "accepted" "open"))} "5 answers"]
          [:span {:class "views"} "311 views"]]
         [:div {:class "title"}
          [:a {:href "http://"} title]]
         [:ul {:class "tags"}
          [:li "python"]
          [:li "pandas"]]
         [:div {:class "author"}
          [:a {:href "http://"} "asked yesterday"]
          " by "
          [:a {:href "http://"} "username"]]
         [:div {:class "answer"}
          [:a {:href "http://"} "accepted answer provided yesterday"]
          " by "
          [:a {:href "http://"} "username"]]
         ]))

(defn html-answer [{:keys [question answer] {:keys [title]} :question}]
  (println question)
  (println answer)
  (html [:div {:class (str "stackover-question" "selected")}
         [:div {:class "meta"}
          [:span {:class "votes"} "11 votes"]
          [:span {:class (str "answers" " " (if true "accepted" "open"))} "5 answers"]
          [:span {:class "views"} "311 views"]]
         [:div {:class "title"}
          [:a {:href "http://"} title]]
         [:ul {:class "tags"}
          [:li "python"]
          [:li "pandas"]]
         [:div {:class "body"} "question body here"]
         [:div {:class "author"}
          [:a {:href "http://"} "asked yesterday"]
          " by "
          [:a {:href "http://"} "username"]]
         [:div {:class "answer"}
          [:div {:class "body"} "answer body here"]
          [:a {:href "http://"} "accepted answer provided yesterday"]
          " by "
          [:a {:href "http://"} "username"]]]))

(defn oembed-question [question]
  {:version "1.0"
   :type "rich"
   :author_name (-> question :question :owner :display_name)
   :author_url (str "http://stackoverflow.com/users/" (-> question :question :owner :user_id))
   :provider_name "StackOverflow"
   :provider_url "http://www.stackoverflow.com/"
   :html (html-question question)})

(defn oembed-answer [answer]
  {:version "1.0"
   :type "rich"
   :author_name (-> answer :answers first :owner :display_name)
   :author_url (str "http://stackoverflow.com/users/" (-> answer :answers first :owner :user_id))
   :provider_name "StackOverflow"
   :provider_url "http://www.stackoverflow.com/"
   :html (html-answer answer)})

(defn error [url id]
  {:error (str url " " "is not a valid StackOverflow URL")
   :id id})

(defn main [params]
  (def id (re-find #"http://stackoverflow.com/questions/([\d]{4,})/[^/]+/?([\d]{4,})?.*" (:url params)))
  (def questionid (nth id 1))
  (def answerid (nth id 2 false))
  (cond
    answerid (p/then (full-answer answerid questionid (:key params)) oembed-answer)
    questionid (p/then (full-question questionid (:key params) #(:is_accepted %)) oembed-question)
    :else (error (:url params) id)))

(defn clj-promise->js [o]
  (if (p/promise? o)
    (p/then o (fn [r] (p/resolved (clj->js r))))
    (clj->js o)))

(set! js/main (fn [args] (clj-promise->js (main (js->clj args :keywordize-keys true)))))
