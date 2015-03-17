(ns echo3.core
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:gen-class))

(def cookie (slurp "amazon.cookie.txt"))
(def headers {"Cookie" cookie
              "User-Agent" "User Agent/0.0.1"
              "Content-Type" "application/json"
              "Origin" "http://echo.amazon.com"
              "Referer" "http://echo.amazon.com/spa/index.html"
              })

(def vera-ip "192.168.1.222")

(def scenes
{"lights on" 11
"lights" 11
"turn on lights" 11
"turn lights on" 11
"lights off" 12
"turn lights off" 12
"music" 14
"music off" 15
"lock door" 25
"lock doors" 25
"unlock door" 24
"unlock doors" 24
"folk music" 22
"goldroom music" 23
"gold room music" 23})

(defn activate-scene [num]
  (client/get (str "http://" vera-ip ":3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum=" num)))

(defn get-tasks []
  (let [url "https://pitangui.amazon.com/api/todos?type=TASK&size=1"
       res (client/get url {:headers headers})]
    (first ((json/read-str (res :body)) "values"))))

(def last-updated (atom ((get-tasks) "createdDate")))

(defn set-interval [callback ms]
  (future (while true (do (Thread/sleep ms) (callback)))))

(defn check-scenes []
  (let [task (get-tasks)
       lu (task "createdDate")
       items (map clojure.string/lower-case (cons (task "text") (task "nbesItems")))
       text (first (filter #(scenes %) items))]
    (if (> lu @last-updated)
        (do
          (println items)
          (if-let [num (scenes text)] (activate-scene num))
          (reset! last-updated lu))
        (print "."))))

(defn -main [& args]
  (do
    (println (get-tasks))
    (set-interval check-scenes 200)
    (Thread/sleep (* 365 24 60 60 1000))))
