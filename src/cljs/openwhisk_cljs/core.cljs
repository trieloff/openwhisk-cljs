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
                                    :filter defaultfilter
                                    :key key}})
          (fn [response]
            (p/resolved (:items (gzjson (:body response) (get (-> response :headers) "Content-Length")))))))

(defn full-question
  "Gets question `id` including all answers. Optionally, filter answers with
  function `pred`."
  ([id key]
   (let [q (p/all [(question id key)
                   (answers id key)])]
     (p/then q (fn [[questn answrs]] {:question questn
                                      :accepted (first (filter #(:is_accepted %) answrs))
                                      :top (last (sort-by :score answrs))
                                      :first (first (sort-by :creation_date answrs))})))))

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
                                    :answer   (first answr)}))))

(defn pretty-date [d]
  (let [days (js/Math.round (/ (- d (/ (.getTime (js/Date.)) 1000)) (* -1 60 60 24)))]
    (cond
      (= 0 days) "today"
      (= 1 days) "yesterday"
      :else (str days " days ago"))))

(defn html-question [{:keys [question accepted top first]
                      {:keys [title link score view_count answer_count tags owner creation_date]} :question}]
  (html [:div {:class (str "stackoverflow-question" " " (if accepted "accepted" "open"))}
         [:div.meta
          [:span.votes (str score " votes")]
          [:span {:class (str "answers" " " (if accepted "accepted" "open"))} (str answer_count " answers")]
          [:span.views (str view_count " views")]]
         [:div.title
          [:a {:href link} title]]
         [:ul.tags
          (map #(vector :li %) tags)]
         [:div.author
          [:a {:href (:link owner)} (:display_name owner)]
          " "
          [:a {:href link} "asked " (pretty-date creation_date)]
          [:a {:href "http://"} "username"]]
         [:div.body (:body question)]
         (if accepted
           [:div.answer
            [:a {:href (str link "/" (:answer_id accepted))} "accepted answer provided " (pretty-date (:creation_date accepted))]
            " by "
            [:a {:href (:link (:owner accepted))} (:display_name (:owner accepted))]
            [:div.body (:body accepted)]]
           [:div.answer
            [:a {:href (str link "/" (:answer_id top))} "top answer provided " (pretty-date (:creation_date top))]
            " by "
            [:a {:href (:link (:owner top))} (:display_name (:owner top))]])
         ]))

(defn html-answer [{:keys [question answer]
                    {:keys [title link score view_count answer_count tags owner creation_date]} :question}]
  (html [:div {:class (str "stackoverflow-question" " " "featured")}
         [:div.meta
          [:span.votes (str score " votes")]
          [:span {:class (str "answers" " " "selected")} (str answer_count " answers")]
          [:span.views (str view_count " views")]]
         [:div.title
          [:a {:href link} title]]
         [:ul.tags
          (map #(vector :li %) tags)]
         [:div.author
          [:a {:href (:link owner)} (:display_name owner)]
          " "
          [:a {:href link} "asked " (pretty-date creation_date)]
          [:a {:href "http://"} "username"]]
         [:div.body (:body question)]
         [:div.answer
          [:a {:href (str link "/" (:answer_id question))} "featured answer provided " (pretty-date (:creation_date question))]
          " by "
          [:a {:href (:link (:owner answer))} (:display_name (:owner answer))]
          [:div.body (:body answer)]]
         ]))

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
  (try
    (if (nil? (:url params))
      {:version "1.0"
       :test    "hey"
       :params  params
       :error   "You need to specify a URL to embed. Use the `url` parameter."}
      (let [id (re-find #"https?://stackoverflow.com/questions/([\d]{4,})/[^/]+/?([\d]{4,})?.*" (:url params))
            questionid (nth id 1)
            answerid (nth id 2 false)]
        (cond
          questionid {:q "q"}
          :else (p/promise {:hello      "world"
                     :questionid questionid
                     :answerid   answerid
                     :id         id
                     :params     params
                     :out        (cond
                                   answerid "answer"
                                   questionid (p/then (full-question questionid (:key params)) identity)
                                   :else error)}))))
    (catch :default e {:exception e
                       :params params})))

(defn main2 [params]
  (try
    (if (nil? (:url params))
     {:version "1.0"
      :test    "hey"
      :params  params
      :error   "You need to specify a URL to embed. Use the `url` parameter."}
     (let [id (re-find #"https?://stackoverflow.com/questions/([\d]{4,})/[^/]+/?([\d]{4,})?.*" (:url params))
           questionid (nth id 1)
           answerid (nth id 2 false)]
       (cond
         answerid (p/then (full-answer answerid questionid (:key params)) oembed-answer)
         questionid (p/then (full-question questionid (:key params)) oembed-question)
         :else (error (:url params) id))))
    (catch :default e {:exception e
                        :params params})))

(defn clj-promise->js [o]
  (if (p/promise? o)
    (p/then o (fn [r] (p/resolved (clj->js r))))
    (clj->js o)))

(set! js/main (fn [args] (clj-promise->js (main (js->clj args :keywordize-keys true)))))
