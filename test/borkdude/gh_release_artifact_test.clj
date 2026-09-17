(ns borkdude.gh-release-artifact-test
  (:require
   [babashka.fs :as fs]
   [babashka.http-client :as http]
   [borkdude.gh-release-artifact.internal :as ghr]
   [cheshire.core :as cheshire]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]))

(def ^:private upload-url "https://uploads.example/assets")

(defn- asset [nm]
  {:name nm :url (str "https://api.example/assets/" nm)})

(defn- steady
  "A temporary name with its uuid taken out, so a log is comparable."
  [s]
  (-> s
      (str/replace #"\.upload-[0-9a-f-]{36}" ".upload")
      (str/replace #"\.replaced-[0-9a-f-]{36}" ".replaced")))

(def ^:private created (constantly 201))
(def ^:private renamed (constantly 200))
(def ^:private created? (fn [status] (= 201 status)))

(defn- call
  "Calls overwrite-asset against a fake release. Returns the requests it made
  as [op name] pairs under :log, the assets the release ends up with under
  :assets, and what it threw under :error.

  assets is what the release holds already. post-status takes the asset name
  and the number of earlier posts, and returns the status Github answers
  with. patch-status does the same for a rename. post-creates? says whether
  a post put the asset on the release, which Github can do and then answer
  an error anyway."
  [{:keys [assets post-status patch-status post-creates? opts]
    :or {assets [] post-status created patch-status renamed post-creates? created?}}]
  ;; post-status may answer with a status or with a whole response map, so a
  ;; test can send headers along
  (let [log (atom [])
        posts (atom 0)
        patches (atom 0)
        ;; the release, by asset url, so a rename and a delete move the same
        ;; asset the real API would
        state (atom (into {} (map (juxt :url :name)) assets))
        file (doto (fs/file (fs/create-temp-dir) "artifact.zip") (spit "bytes"))
        record (fn [op nm] (swap! log conj [op (steady nm)]))
        error (atom nil)]
    (with-redefs-fn
      {#'ghr/release-for (fn [_] {:upload_url (str upload-url "{?name,label}")})
       #'ghr/list-assets (fn [_] (mapv (fn [[url nm]] {:name nm :url url}) @state))
       #'http/post (fn [_url {:keys [query-params]}]
                     (let [nm (get query-params "name")
                           answer (post-status nm @posts)
                           answer (if (map? answer) answer {:status answer})
                           status (:status answer)]
                       (swap! posts inc)
                       (record :post nm)
                       (when (post-creates? status)
                         (swap! state assoc (:url (asset nm)) nm))
                       (assoc answer :body (cheshire/generate-string (asset nm)))))
       #'http/delete (fn [url _]
                       (record :delete (get @state url (fs/file-name url)))
                       (swap! state dissoc url)
                       {:status 204})
       #'http/patch (fn [url {:keys [body]}]
                      (let [nm (:name (cheshire/parse-string body true))
                            status (patch-status nm @patches)]
                        (swap! patches inc)
                        (record :patch (str (steady (get @state url (fs/file-name url)))
                                            " -> " (steady nm)))
                        (when (= 200 status)
                          (swap! state assoc url nm))
                        {:status status
                         :body (cheshire/generate-string (asset nm))}))}
      #(try (ghr/overwrite-asset (merge {:file (str file)
                                         :org "o" :repo "r" :tag "v1"
                                         :retry-pause-ms 1}
                                        opts))
            (catch Exception e (reset! error e))))
    {:log @log
     :assets (sort (map steady (vals @state)))
     :error @error}))

(deftest upload-a-new-asset
  (testing "an asset the release does not have yet is uploaded under its own name"
    (let [{:keys [log assets]} (call {})]
      (is (= [[:post "artifact.zip"]] log))
      (is (= ["artifact.zip"] assets)))))

(deftest replace-an-existing-asset
  (testing "the bytes go up, the existing asset moves aside, the new one takes the name"
    (let [{:keys [log assets]} (call {:assets [(asset "artifact.zip")]})]
      (is (= [[:post "artifact.zip.upload"]
              [:patch "artifact.zip -> artifact.zip.replaced"]
              [:patch "artifact.zip.upload -> artifact.zip"]
              [:delete "artifact.zip.replaced"]]
             log))
      (is (= ["artifact.zip"] assets))))
  (testing "a failed upload keeps the existing asset"
    (let [{:keys [assets error]} (call {:assets [(asset "artifact.zip")]
                                        :post-status (constantly 500)})]
      (is (re-find #"failed with status 500" (ex-message error)))
      (is (= ["artifact.zip"] assets))))
  (testing "a failed rename puts the existing asset back and takes the upload away"
    (let [{:keys [assets error]} (call {:assets [(asset "artifact.zip")]
                                        ;; the move aside and the roll back
                                        ;; go through, the rename between
                                        ;; them does not
                                        :patch-status (fn [_ n] (if (= 1 n) 422 200))})]
      (is (= "Renaming artifact.zip failed with status 422" (ex-message error)))
      (is (= ["artifact.zip"] assets))))
  (testing "a rename of the existing asset that fails takes the upload away"
    (let [{:keys [assets error]} (call {:assets [(asset "artifact.zip")]
                                        :patch-status (constantly 422)})]
      (testing "and names the asset the caller knows, not the one it moves to"
        (is (= "Renaming artifact.zip failed with status 422" (ex-message error))))
      (is (= ["artifact.zip"] assets))))
  (testing "a roll back that fails says where the bytes are"
    (let [{:keys [assets error]} (call {:assets [(asset "artifact.zip")]
                                        ;; the move aside goes through and
                                        ;; every rename after it fails
                                        :patch-status (fn [_ n] (if (zero? n) 200 422))})]
      (is (re-find #"The release has no artifact.zip now: its bytes are under artifact.zip.replaced-"
                   (ex-message error)))
      (is (false? (:rolled-back (ex-data error))))
      (is (= ["artifact.zip.replaced"] assets)))))

(deftest retry-what-github-says-to-retry
  (testing "a 5xx is retried, and the asset an earlier attempt left is cleared first"
    (let [{:keys [log assets]} (call {:post-status (fn [_ n] (if (zero? n) 500 201))
                                      :post-creates? (constantly true)})]
      (is (= [[:post "artifact.zip"] [:delete "artifact.zip"] [:post "artifact.zip"]] log))
      (is (= ["artifact.zip"] assets))))
  (testing "a 429 is retried"
    (is (= [[:post "artifact.zip"] [:delete "artifact.zip"] [:post "artifact.zip"]]
           (:log (call {:post-status (fn [_ n] (if (zero? n) 429 201))
                        :post-creates? (constantly true)})))))
  (testing "an upload that gives up clears the asset its attempts may have created"
    (let [{:keys [assets error]} (call {:post-status (constantly 500)
                                        :post-creates? (constantly true)})]
      (is (re-find #"failed with status 500" (ex-message error)))
      (is (= [] assets))))
  (testing "a 4xx that is not a rate limit is not retried"
    (let [{:keys [log error]} (call {:post-status (constantly 404)})]
      (is (= [[:post "artifact.zip"]] log))
      (is (= "Uploading artifact.zip failed with status 404" (ex-message error)))))
  (testing "the status and the asset name are in the message"
    (is (= "Uploading artifact.zip failed with status 422"
           (ex-message (:error (call {:post-status (constantly 422)})))))))

(deftest read-the-retry-after-header
  (let [ms #'ghr/retry-after-ms]
    (testing "the header is read whatever case the server sent it in"
      (is (= 5000 (ms {:headers {"retry-after" "5"}})))
      (is (= 5000 (ms {:headers {"Retry-After" "5"}})))
      (is (= 5000 (ms {:headers {:Retry-After " 5 "}}))))
    (testing "a pause longer than a minute is cut down to one"
      (is (= 60000 (ms {:headers {"retry-after" "3600"}}))))
    (testing "nothing to wait for"
      (is (nil? (ms {:headers {}})))
      (is (nil? (ms {})))
      ;; Github may send a date instead, which this does not read
      (is (nil? (ms {:headers {"retry-after" "Wed, 21 Oct 2026 07:28:00 GMT"}}))))))

(deftest a-rate-limit-is-retried
  (testing "a 403 that asks to come back is retried"
    (is (= [[:post "artifact.zip"] [:delete "artifact.zip"] [:post "artifact.zip"]]
           (:log (call {:post-status (fn [_ n] (if (zero? n)
                                                 {:status 403 :headers {"Retry-After" "0"}}
                                                 201))
                        :post-creates? (constantly true)})))))
  (testing "a 403 that does not ask is a permission error, so it stands"
    (let [{:keys [log error]} (call {:post-status (constantly 403)})]
      (is (= [[:post "artifact.zip"]] log))
      (is (= "Uploading artifact.zip failed with status 403" (ex-message error))))))

(deftest options-take-an-explicit-nil
  (testing "a nil where a number would go falls back to the default"
    (is (nil? (:error (call {:opts {:retries nil :retry-pause-ms nil}}))))))

(deftest upload-a-checksum
  (testing "the checksum follows the file it describes"
    (is (= [[:post "artifact.zip"] [:post "artifact.zip.sha256"]]
           (:log (call {:opts {:sha256 true}})))))
  (testing "an existing checksum is replaced the same way as the file"
    (let [{:keys [log assets]} (call {:assets [(asset "artifact.zip")
                                               (asset "artifact.zip.sha256")]
                                      :opts {:sha256 true}})]
      (is (= [[:post "artifact.zip.upload"]
              [:patch "artifact.zip -> artifact.zip.replaced"]
              [:patch "artifact.zip.upload -> artifact.zip"]
              [:delete "artifact.zip.replaced"]
              [:post "artifact.zip.sha256.upload"]
              [:patch "artifact.zip.sha256 -> artifact.zip.sha256.replaced"]
              [:patch "artifact.zip.sha256.upload -> artifact.zip.sha256"]
              [:delete "artifact.zip.sha256.replaced"]]
             log))
      (is (= ["artifact.zip" "artifact.zip.sha256"] assets))))
  (testing "a failed file upload leaves the checksum of the old file in place"
    (let [{:keys [log error]} (call {:assets [(asset "artifact.zip")
                                              (asset "artifact.zip.sha256")]
                                     :post-status (constantly 500)
                                     :opts {:sha256 true}})]
      (is (some? error))
      (is (= [] (filter (fn [[_ nm]] (str/includes? nm "sha256")) log))))))

(deftest keep-an-asset-without-overwrite
  (testing "an existing asset is left as it is"
    (is (= [] (:log (call {:assets [(asset "artifact.zip")] :opts {:overwrite false}}))))))
