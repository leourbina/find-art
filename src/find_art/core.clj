(ns find-art.core
(:require [clj-http.client :as client]
          [clojure.java.io :as io]
          [diehard.core :as dh])
  (:gen-class))


(dh/defretrypolicy policy
  {:delay-ms 750
   :retry-if (fn [return-value exception]
               (let [error? (= 500 (-> exception ex-data :status))]
                 (if error?
                   (println "Retrying")
                   error?)))})

(defn get-page
  [time]
  (try
    (let [url (str "https://permanent-redirect.xyz/pages/" time)
          res (dh/with-retry {:policy policy}
                (client/get (str "https://permanent-redirect.xyz/pages/" time) {:insecure? true}))]
      res)
    (catch Exception e nil)))


(defn find-art []
  (let [start-time (int (/ (System/currentTimeMillis) 1000))]
   (loop [time start-time]
     (println "Looking backwards from time" time)
     (if-let [res (get-page time)]
       (with-open [out-file (io/writer (str "/tmp/art-contents/latest.html"))]
         (println "Got it!!")
         (.write out-file (res :body))
         (.flush out-file))
       (recur (dec time))))))


(defn -main
  "I find art"
  [& args]
  (find-art))
