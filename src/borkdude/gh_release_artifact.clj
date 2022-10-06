(ns borkdude.gh-release-artifact
  (:require
   [babashka.curl :as curl]
   [babashka.fs :as fs]
   [cheshire.core :as cheshire]
   [clj-commons.digest :as digest]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]
   [borkdude.gh-release-artifact.internal :as ghr]))

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

(defn ^:no-doc overwrite-asset [opts]
  (ghr/overwrite-asset opts))

(defn release-artifact
  "Uploads artifact to github release. Creates (draft) release if there is
  no existing release yet.

  Required options:

  * `:org` - Github organization.
  * `:repo` - Github repository.
  * `:tag` - Tag of release.
  * `:file` - The file to be uploaded.

  Optional options:

  * `:commit` - Commit to be associated with release. Defaults to current commit.
  * `:sha256` - Upload a `file.sha256` hash file along with `:file`.
  * `:overwrite` - Overwrite exiting upload. Defaults to `false`.
  * `:draft` - Created draft release. Defaults to `true`.
  * `:content-type` - The file's content type. Default to lookup by extension in `default-mime-types`."
  [{:keys [overwrite]
    :or {overwrite false}
    :as opts}]
  (overwrite-asset (assoc opts :overwrite overwrite)))

(comment
  (release-artifact {:org "borkdude"
                     :repo "test-repo"
                     :tag "v0.0.15"
                     :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                     :file "README.md"
                     :sha256 true
                     :overwrite true})

  (release-artifact {:org "borkdude"
                    :repo "test-repo"
                    :tag "v0.0.15"
                    :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                    :file "README.md"
                    :overwrite false})
  )
