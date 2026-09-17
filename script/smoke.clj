(ns smoke
  "Runs release-artifact against a real Github release, because the test
  suite stubs every call and cannot say whether Github behaves the way the
  upload path assumes: that it refuses a second asset of one name, that a
  rename into a name freed a moment earlier goes through, and that the asset
  list shows what an upload just created.

  Writes to a draft release, tag smoke-test, on the repo given as the first
  argument. Needs GITHUB_TOKEN with write access to it.

      bb smoke borkdude/test-repo"
  (:require
   [babashka.fs :as fs]
   [babashka.http-client :as http]
   [borkdude.gh-release-artifact :as ghr]
   [borkdude.gh-release-artifact.internal :as internal]
   [clj-commons.digest :as digest]
   [clojure.string :as str]))

(def tag "smoke-test")

(defn- assets [opts]
  (->> (internal/list-assets opts)
       (map (juxt :name :size))
       (into {})))

(defn- asset-body
  "The bytes of an asset of a draft release, which are readable only with the
  token that can see the release."
  [opts nm]
  (let [asset (some #(when (= nm (:name %)) %) (internal/list-assets opts))]
    (-> (http/get (:url asset)
                  {:headers {"Authorization" (str "token " (System/getenv "GITHUB_TOKEN"))
                             "Accept" "application/octet-stream"}})
        :body)))

(defn- check [what expected actual]
  (if (= expected actual)
    (println "  ok  " what)
    (do (println "  FAIL" what "\n    expected:" (pr-str expected) "\n    actual:  " (pr-str actual))
        (throw (ex-info "smoke test failed" {:check what})))))

(defn -main [& args]
  (let [[org repo] (str/split (or (first args) "borkdude/test-repo") #"/")
        _ (assert repo "Give the repo as org/repo")
        opts {:org org :repo repo :tag tag :draft true :overwrite true :sha256 true}
        dir (fs/create-temp-dir)
        file (fs/file dir "smoke.txt")
        upload (fn [content]
                 (spit file content)
                 (ghr/release-artifact (assoc opts :file (str file))))]

    (println "uploading to" (str org "/" repo) "tag" tag)

    (println "a new asset")
    (upload "one")
    (check "the asset is there, at its own size" 3 (get (assets opts) "smoke.txt"))
    (check "its checksum is there" 64 (get (assets opts) "smoke.txt.sha256"))

    (println "replacing it")
    (upload "twotwo")
    (let [now (assets opts)]
      (check "the asset carries the new bytes" 6 (get now "smoke.txt"))
      (check "no temporary asset is left behind"
             [] (filterv #(or (str/includes? % ".upload-") (str/includes? % ".replaced-"))
                         (keys now)))
      (check "the checksum is the one of the new bytes"
             (digest/sha-256 (fs/file file))
             (str/trim (asset-body opts "smoke.txt.sha256"))))

    (println "replacing it again")
    (upload "three")
    (check "the asset carries the newest bytes" 5 (get (assets opts) "smoke.txt"))

    (println "\nall checks passed. The draft release is at"
             (str "https://github.com/" org "/" repo "/releases"))))
