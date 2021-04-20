(ns borkdude.gh-release-artifact
  (:require
   [babashka.curl :as curl]
   [babashka.fs :as fs]
   [cheshire.core :as cheshire]
   [clojure.string :as str]))

(def token (System/getenv "GITHUB_TOKEN"))

(def endpoint "https://api.github.com")

(defn path [& strs]
  (str/join "/" strs))

(def release-endpoint (path endpoint "repos" "borkdude" "test-repo" "releases"))

(defn with-gh-headers [m]
  (update m :headers assoc
          "Authorization" (str "token " token)
          "Accept" "application/vnd.github.v3+json"))

(defn list-releases []
  (-> (curl/get release-endpoint
                (with-gh-headers {}))
      :body
      (cheshire/parse-string true)))

(defn get-draft-release [tag]
  (some #(when (= tag (:tag_name %)) %)
        (list-releases)))

(defn create-draft-release [{:keys [:tag :commit]}]
  (-> (curl/post release-endpoint
                 (with-gh-headers
                   {:body (cheshire/generate-string {:tag_name tag
                                                     :target_commitish commit
                                                     :name tag
                                                     :draft true})}))
      :body
      (cheshire/parse-string true)))

(defn -draft-release-for [{:keys [:tag] :as opts}]
  (or (get-draft-release tag)
      (create-draft-release opts)))

(def draft-release-for (memoize -draft-release-for))

(defn list-assets [opts]
  (let [release (draft-release-for opts)]
    (-> (curl/get (:assets_url release) (with-gh-headers {}))
        :body
        (cheshire/parse-string true))))

(defn overwrite-asset [{:keys [:file] :as opts}]
  (let [release (draft-release-for opts)
        upload-url (:upload_url release)
        upload-url (str/replace upload-url "{?name,label}" "")
        assets (list-assets opts)
        file-name (fs/file-name file)
        asset (some #(when (= file-name (:name %)) %) assets)]
    (when asset
      (curl/delete (:url asset) (with-gh-headers {})))
    (-> (curl/post upload-url
                   {:throw false
                    :query-params {"name" (fs/file-name file)
                                   "label" (fs/file-name file)}
                    :headers {"Authorization" (str "token " token)
                              "Content-Type" "application/zip"}
                    :body (fs/file file)})
        :body
        (cheshire/parse-string true))))

(comment
 (overwrite-asset {:tag "v0.0.1"
                   :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                   :file "artifacts/foo.zip"}))
