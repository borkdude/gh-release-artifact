(ns borkdude.gh-release-artifact
  (:require
   [babashka.curl :as curl]
   [babashka.fs :as fs]
   [cheshire.core :as cheshire]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]))

(def token (System/getenv "GITHUB_TOKEN"))

(def endpoint "https://api.github.com")

(defn path [& strs]
  (str/join "/" strs))

(defn release-endpoint [org repo]
  (path endpoint "repos" org repo "releases"))

(defn with-gh-headers [m]
  (update m :headers assoc
          "Authorization" (str "token " token)
          "Accept" "application/vnd.github.v3+json"))

(defn list-releases [org repo]
  (-> (curl/get (release-endpoint org repo)
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
  (-> (curl/post (release-endpoint org repo)
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
  (curl/delete (path (release-endpoint org repo) id)))

(defn -release-for [{:keys [:org :repo :tag] :as opts}]
  (or (get-draft-release org repo tag)
      (let [resp (create-release opts)
            created-id (:id resp)
            release (get-draft-release org repo tag)
            release-id (:id release)]
        (when-not (= created-id release-id)
          ;; in this scenario some other process created a new release just before username
          (delete-release (assoc opts :id created-id)))
        release)))

(def release-for (memoize -release-for))

(defn list-assets [opts]
  (let [release (release-for opts)]
    (-> (curl/get (:assets_url release) (with-gh-headers {}))
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

(defn overwrite-asset [{:keys [:file :content-type] :as opts}]
  (let [release (release-for opts)
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
                              "Content-Type"
                              (or content-type
                                  (get default-mime-types (fs/extension file)))}
                    :body (fs/file file)})
        :body
        (cheshire/parse-string true))))

(comment
  (overwrite-asset {:org "borkdude"
                    :repo "test-repo"
                    :tag "v0.0.1"
                    :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                    :file "artifacts/foo.zip"
                    :content-type "application/zip"}))
