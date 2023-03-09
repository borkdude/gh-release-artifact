(ns borkdude.gh-release-artifact
  (:require
   [borkdude.gh-release-artifact.internal :as ghr]))

;; A simple mime type utility from https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/util/mime_type.clj
(def ^{:doc "A map of file extensions to mime-types."}
  default-mime-types
  ghr/default-mime-types)

(defn ^:no-doc overwrite-asset [opts]
  (ghr/overwrite-asset opts))

(defn release-artifact
  "Uploads artifact to github release. Creates (draft) release if there
  is no existing release yet. Uses token from `GITHUB_TOKEN`
  environment variable for auth.

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
                     :tag "v0.0.16"
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
