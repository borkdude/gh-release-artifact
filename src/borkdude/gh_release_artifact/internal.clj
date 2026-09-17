(ns borkdude.gh-release-artifact.internal
  {:no-doc true}
  (:require
   [babashka.http-client :as http]
   [babashka.fs :as fs]
   [cheshire.core :as cheshire]
   [clj-commons.digest :as digest]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]))

(def token #(System/getenv "GITHUB_TOKEN"))

(def endpoint "https://api.github.com")

(defn path [& strs]
  (str/join "/" strs))

(defn release-endpoint [org repo]
  (path endpoint "repos" org repo "releases"))

(defn with-gh-headers [m]
  (update m :headers assoc
          "Authorization" (str "token " (token))
          "Accept" "application/vnd.github.v3+json"))

(defn list-releases [org repo]
  (-> (http/get (release-endpoint org repo)
                (with-gh-headers {}))
      :body
      (cheshire/parse-string true)))

(defn get-draft-release [org repo tag]
  (some #(when (= tag (:tag_name %)) %)
        ;; always choose oldest release to prevent race condition
        (reverse (list-releases org repo))))

(defn current-commit []
  (-> (sh "git" "rev-parse" "HEAD")
      :out
      str/trim))

(defn create-release [{:keys [:tag :commit :org :repo :draft
                              :target-commitish :prerelease]
                       :or {draft true
                            target-commitish (or commit
                                                 (current-commit))}}]
  (-> (http/post (release-endpoint org repo)
                 (with-gh-headers
                   {:body
                    (cheshire/generate-string (cond-> {:tag_name tag
                                                       :name tag
                                                       :draft draft}
                                                target-commitish
                                                (assoc :target_commitish target-commitish)
                                                prerelease
                                                (assoc :prerelease prerelease)))}))
      :body
      (cheshire/parse-string true)))

(defn delete-release [{:keys [:org :repo :id]}]
  (http/delete (path (release-endpoint org repo) id) {:throw false}))

(defn -release-for [{:keys [:org :repo :tag] :as opts}]
  (or (get-draft-release org repo tag)
      (let [resp (create-release opts)
            created-id (:id resp)
            release (loop [attempt 0]
                      (when (< attempt 10)
                        (Thread/sleep (* attempt 50))
                        ;; eventual consistency...
                        (if-let [dr (get-draft-release org repo tag)]
                          dr
                          (recur (inc attempt)))))
            release-id (:id release)]
        (when-not (= created-id release-id)
          ;; in this scenario some other process created a new release just before username
          (delete-release (assoc opts :id created-id)))
        release)))

(def release-for (memoize -release-for))

(defn list-assets [opts]
  (let [release (release-for opts)]
    (-> (http/get (:assets_url release) (with-gh-headers {}))
        :body
        (cheshire/parse-string true))))

;; A simple mime type utility from https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/util/mime_type.clj
(def ^{:doc "A map of file extensions to mime-types."}
  default-mime-types
  {"7z"       "application/x-7z-compressed"
   "aac"      "audio/aac"
   "ai"       "application/postscript"
   "appcache" "text/cache-manifest"
   "asc"      "text/plain"
   "atom"     "application/atom+xml"
   "avi"      "video/x-msvideo"
   "bin"      "application/octet-stream"
   "bmp"      "image/bmp"
   "bz2"      "application/x-bzip"
   "class"    "application/octet-stream"
   "cer"      "application/pkix-cert"
   "crl"      "application/pkix-crl"
   "crt"      "application/x-x509-ca-cert"
   "css"      "text/css"
   "csv"      "text/csv"
   "deb"      "application/x-deb"
   "dart"     "application/dart"
   "dll"      "application/octet-stream"
   "dmg"      "application/octet-stream"
   "dms"      "application/octet-stream"
   "doc"      "application/msword"
   "dvi"      "application/x-dvi"
   "edn"      "application/edn"
   "eot"      "application/vnd.ms-fontobject"
   "eps"      "application/postscript"
   "etx"      "text/x-setext"
   "exe"      "application/octet-stream"
   "flv"      "video/x-flv"
   "flac"     "audio/flac"
   "gif"      "image/gif"
   "gz"       "application/gzip"
   "htm"      "text/html"
   "html"     "text/html"
   "ico"      "image/x-icon"
   "iso"      "application/x-iso9660-image"
   "jar"      "application/java-archive"
   "jpe"      "image/jpeg"
   "jpeg"     "image/jpeg"
   "jpg"      "image/jpeg"
   "js"       "text/javascript"
   "json"     "application/json"
   "lha"      "application/octet-stream"
   "lzh"      "application/octet-stream"
   "mov"      "video/quicktime"
   "m3u8"     "application/x-mpegurl"
   "m4v"      "video/mp4"
   "mjs"      "text/javascript"
   "mp3"      "audio/mpeg"
   "mp4"      "video/mp4"
   "mpd"      "application/dash+xml"
   "mpe"      "video/mpeg"
   "mpeg"     "video/mpeg"
   "mpg"      "video/mpeg"
   "oga"      "audio/ogg"
   "ogg"      "audio/ogg"
   "ogv"      "video/ogg"
   "pbm"      "image/x-portable-bitmap"
   "pdf"      "application/pdf"
   "pgm"      "image/x-portable-graymap"
   "png"      "image/png"
   "pnm"      "image/x-portable-anymap"
   "ppm"      "image/x-portable-pixmap"
   "ppt"      "application/vnd.ms-powerpoint"
   "ps"       "application/postscript"
   "qt"       "video/quicktime"
   "rar"      "application/x-rar-compressed"
   "ras"      "image/x-cmu-raster"
   "rb"       "text/plain"
   "rd"       "text/plain"
   "rss"      "application/rss+xml"
   "rtf"      "application/rtf"
   "sgm"      "text/sgml"
   "sgml"     "text/sgml"
   "svg"      "image/svg+xml"
   "swf"      "application/x-shockwave-flash"
   "tar"      "application/x-tar"
   "tif"      "image/tiff"
   "tiff"     "image/tiff"
   "ts"       "video/mp2t"
   "ttf"      "font/ttf"
   "txt"      "text/plain"
   "md"       "text/plain"
   "vsix"     "application/vsix"
   "webm"     "video/webm"
   "wmv"      "video/x-ms-wmv"
   "woff"     "font/woff"
   "woff2"    "font/woff2"
   "xbm"      "image/x-xbitmap"
   "xls"      "application/vnd.ms-excel"
   "xml"      "text/xml"
   "xpm"      "image/x-xpixmap"
   "xwd"      "image/x-xwindowdump"
   "zip"      "application/zip"})

;; Github asks for at most a minute on a secondary rate limit. A header that
;; asks for longer is a reason to stop, not to hold the build there.
(def ^:private max-retry-pause-ms 60000)

(defn- header
  "The value of header nm, whatever case the server sent it in."
  [response nm]
  (some (fn [[k v]] (when (.equalsIgnoreCase (str (name k)) nm) v))
        (:headers response)))

(defn- retry-after-ms
  "The pause a rate limit asks for, in milliseconds, at most a minute. nil
  when the answer carries no Retry-After, or one that is not a number of
  seconds."
  [response]
  (when-let [v (header response "retry-after")]
    (try (min max-retry-pause-ms (* 1000 (Long/parseLong (str/trim v))))
         (catch Exception _ nil))))

(defn- retriable?
  "True for an answer that says to come back: a server error, or a rate
  limit, which Github reports as 429 or as 403 with Retry-After."
  [response]
  (let [status (:status response)]
    (or (and status (<= 500 status 599))
        (= 429 status)
        (and (= 403 status) (retry-after-ms response)))))

(defn- with-retry
  "Calls f, which returns a response map. Retries a server error, a rate
  limit and an exception, pausing as long as Retry-After asks or longer
  before each attempt. Calls before-retry, when given, before a new
  attempt. Returns the last response, or throws the last exception."
  [f {:keys [retries retry-pause-ms before-retry]}]
  (let [retries (or retries 3)
        retry-pause-ms (or retry-pause-ms 1000)]
    (loop [attempt 1]
      (let [[response error] (try [(f) nil] (catch Exception e [nil e]))]
        (if (and (< attempt (long retries))
                 (or error (retriable? response)))
          (do (Thread/sleep (long (or (retry-after-ms response)
                                      (* attempt (long retry-pause-ms)))))
              (when before-retry (before-retry))
              (recur (inc attempt)))
          (if error (throw error) response))))))

(defn- fail [action asset-name response]
  (throw (ex-info (str action " " asset-name " failed with status " (:status response))
                  {:asset asset-name
                   :status (:status response)
                   :body (:body response)})))

(defn- drop-asset-named
  "Deletes the asset of this name, if the release has one. An attempt that
  failed can still have created it, and Github refuses a second asset of a
  name it already has, so this runs before an upload is tried again. Its own
  failure is left to the upload that follows."
  [asset-name opts]
  (try
    (when-let [asset (some #(when (= asset-name (:name %)) %) (list-assets opts))]
      (http/delete (:url asset) (with-gh-headers {:throw false})))
    (catch Exception _ nil)))

(defn- post-asset
  "Uploads file under asset-name and returns the asset. Throws when Github
  does not create it."
  [upload-url file asset-name content-type opts]
  (let [drop! #(drop-asset-named asset-name opts)
        response (try (with-retry
                        #(http/post upload-url
                                    {:throw false
                                     :query-params {"name" asset-name
                                                    "label" asset-name}
                                     :headers {"Authorization" (str "token " (token))
                                               "Content-Type"
                                               (or content-type
                                                   (get default-mime-types (fs/extension file)))}
                                     :body (fs/file file)})
                        (assoc opts :before-retry drop!))
                      (catch Exception e (drop!) (throw e)))]
    (when-not (= 201 (:status response))
      ;; the answers worth retrying are the ones that can have created the
      ;; asset anyway, so giving up on one means clearing it
      (when (retriable? response) (drop!))
      (fail "Uploading" asset-name response))
    (-> response :body (cheshire/parse-string true))))

(defn- delete-asset
  "Deletes asset. An asset that is already gone counts as deleted."
  [asset opts]
  (let [response (with-retry
                   #(http/delete (:url asset) (with-gh-headers {:throw false}))
                   opts)
        status (:status response)]
    (when-not (or (<= 200 status 299) (= 404 status))
      (fail "Deleting" (:name asset) response))))

(defn- discard
  "Deletes asset, ignoring a failure. Used to clean up while another error
  is on its way out, which is the one that should surface."
  [asset opts]
  (try (delete-asset asset opts) (catch Exception _ nil)))

(defn- rename-asset
  "Gives asset the name asset-name and returns it. reported is the name an
  error message calls the asset by, which is the one the caller knows it as
  rather than the temporary one it carries."
  [asset asset-name reported opts]
  (let [response (with-retry
                   #(http/patch (:url asset)
                                (with-gh-headers
                                  {:throw false
                                   :body (cheshire/generate-string {:name asset-name
                                                                    :label asset-name})}))
                   opts)]
    (when-not (= 200 (:status response))
      (fail "Renaming" reported response))
    (-> response :body (cheshire/parse-string true))))

(defn- replace-asset
  "Uploads file as asset-name and returns the asset. When existing is an
  asset of that name, the new bytes go up under a temporary name, the
  existing asset moves aside under another, and the new asset takes the
  name. A step that fails puts the existing asset back under its name, so
  the bytes on the release are the new ones once the exchange is through
  and the existing ones otherwise.

  Github refuses two assets of one name, so the two renames leave a moment
  in which neither holds it. A failure there is reported with
  :rolled-back false and the name the existing asset is parked under."
  [upload-url file asset-name content-type existing opts]
  (if-not existing
    (post-asset upload-url file asset-name content-type opts)
    (let [suffix (java.util.UUID/randomUUID)
          parked (str asset-name ".replaced-" suffix)
          tmp (post-asset upload-url file (str asset-name ".upload-" suffix)
                          content-type opts)]
      (try (rename-asset existing parked asset-name opts)
           (catch Exception e
             (discard tmp opts)
             (throw e)))
      (let [renamed (try (rename-asset tmp asset-name asset-name opts)
                         (catch Exception e
                           ;; the name is free again, so the existing asset
                           ;; goes back under it
                           (let [back (try (rename-asset existing asset-name asset-name opts)
                                           true
                                           (catch Exception _ false))]
                             (discard tmp opts)
                             (if back
                               (throw e)
                               (throw (ex-info (str (ex-message e)
                                                    ". The release has no " asset-name
                                                    " now: its bytes are under " parked)
                                               (assoc (ex-data e)
                                                      :rolled-back false
                                                      :parked-as parked)
                                               e))))))]
        ;; the new asset holds the name; a leftover copy of the old one is
        ;; not worth failing the release over
        (discard existing opts)
        renamed))))

(defn overwrite-asset [{:keys [:file :content-type] :as opts}]
  (let [release (release-for opts)
        upload-url (:upload_url release)
        upload-url (str/replace upload-url "{?name,label}" "")
        assets (list-assets opts)
        file-name (fs/file-name file)
        asset (some #(when (= file-name (:name %)) %) assets)
        overwrite (get opts :overwrite true)
        sha256 (get opts :sha256)]
    (when (or (not asset)
              ;; in case of asset, overwrite must be true, which it is by default
              overwrite)
      (let [body (replace-asset upload-url file file-name content-type asset opts)]
        (when sha256
          ;; after the file itself, so that a checksum never outlives the
          ;; bytes it describes
          (let [sha256-fname (str file-name ".sha256")
                tmp-dir (fs/create-temp-dir)
                sha256-file (fs/file tmp-dir sha256-fname)
                existing-sha-remote (some #(when (= sha256-fname (:name %)) %) assets)]
            (spit sha256-file (digest/sha-256 (fs/file file)))
            (replace-asset upload-url sha256-file sha256-fname "text/plain"
                           existing-sha-remote opts)))
        body))))

(comment
  (overwrite-asset {:org "borkdude"
                    :repo "test-repo"
                    :tag "v0.0.15"
                    :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                    :file "/Users/borkdude/dev/babashka/logo/babashka-blue-yellow.png"
                    :sha256 true})

  (overwrite-asset {:org "borkdude"
                    :repo "test-repo"
                    :tag "v0.0.15"
                    :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                    :file "README.md"
                    :overwrite false})
  )

