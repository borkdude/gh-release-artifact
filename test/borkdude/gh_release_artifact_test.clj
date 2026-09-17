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
  "A temporary upload name with its uuid taken out, so a log is comparable."
  [s]
  (str/replace s #"\.upload-[0-9a-f-]{36}" ".upload"))

(def ^:private created (constantly 201))

(defn- call
  "Calls overwrite-asset against a fake release. Returns the requests it made
  as [op name] pairs under :log, and what it threw under :error. assets is
  what the release holds already. post-status takes the asset name and the
  number of earlier posts, and returns the status Github answers with."
  [{:keys [assets post-status opts]
    :or {assets [] post-status created}}]
  (let [log (atom [])
        posts (atom 0)
        file (doto (fs/file (fs/create-temp-dir) "artifact.zip") (spit "bytes"))
        record (fn [op nm] (swap! log conj [op (steady nm)]))
        error (atom nil)]
    (with-redefs-fn
      {#'ghr/release-for (fn [_] {:upload_url (str upload-url "{?name,label}")})
       #'ghr/list-assets (fn [_] assets)
       #'http/post (fn [_url {:keys [query-params]}]
                     (let [nm (get query-params "name")
                           status (post-status nm @posts)]
                       (swap! posts inc)
                       (record :post nm)
                       {:status status
                        :body (cheshire/generate-string (asset nm))}))
       #'http/delete (fn [url _]
                       (record :delete (fs/file-name url))
                       {:status 204})
       #'http/patch (fn [url {:keys [body]}]
                      (let [nm (:name (cheshire/parse-string body true))]
                        (record :patch (str (steady (fs/file-name url)) " -> " nm))
                        {:status 200
                         :body (cheshire/generate-string (asset nm))}))}
      #(try (ghr/overwrite-asset (merge {:file (str file)
                                         :org "o" :repo "r" :tag "v1"
                                         :retry-pause-ms 1}
                                        opts))
            (catch Exception e (reset! error e))))
    {:log @log :error @error}))

(deftest upload-a-new-asset
  (testing "an asset the release does not have yet is uploaded under its own name"
    (is (= [[:post "artifact.zip"]] (:log (call {}))))))

(deftest replace-an-existing-asset
  (testing "the bytes go up before the existing asset is deleted"
    (is (= [[:post "artifact.zip.upload"]
            [:delete "artifact.zip"]
            [:patch "artifact.zip.upload -> artifact.zip"]]
           (:log (call {:assets [(asset "artifact.zip")]})))))
  (testing "a failed upload keeps the existing asset"
    (let [{:keys [log error]} (call {:assets [(asset "artifact.zip")]
                                     :post-status (constantly 500)})]
      (is (re-find #"failed with status 500" (ex-message error)))
      (is (= [] (filter (fn [[op nm]] (and (= :delete op) (= "artifact.zip" nm))) log)))))
  (testing "a first attempt that fails with 5xx is retried"
    (is (= [[:post "artifact.zip"] [:post "artifact.zip"]]
           (:log (call {:post-status (fn [_ n] (if (zero? n) 500 201))}))))))

(deftest a-failure-throws
  (testing "the status and the asset name are in the message"
    (is (= "Uploading artifact.zip failed with status 422"
           (ex-message (:error (call {:post-status (constantly 422)}))))))
  (testing "a 4xx is not retried"
    (is (= [[:post "artifact.zip"]]
           (:log (call {:post-status (constantly 404)}))))))

(deftest upload-a-checksum
  (testing "the checksum follows the file it describes"
    (is (= [[:post "artifact.zip"] [:post "artifact.zip.sha256"]]
           (:log (call {:opts {:sha256 true}})))))
  (testing "an existing checksum is replaced the same way as the file"
    (is (= [[:post "artifact.zip.upload"]
            [:delete "artifact.zip"]
            [:patch "artifact.zip.upload -> artifact.zip"]
            [:post "artifact.zip.sha256.upload"]
            [:delete "artifact.zip.sha256"]
            [:patch "artifact.zip.sha256.upload -> artifact.zip.sha256"]]
           (:log (call {:assets [(asset "artifact.zip") (asset "artifact.zip.sha256")]
                        :opts {:sha256 true}})))))
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
