# Table of contents
-  [`borkdude.gh-release-artifact`](#borkdude.gh-release-artifact) 
    -  [`default-mime-types`](#borkdude.gh-release-artifact/default-mime-types) - A map of file extensions to mime-types.
    -  [`release-artifact`](#borkdude.gh-release-artifact/release-artifact) - Uploads artifact to github release.

-----
# <a name="borkdude.gh-release-artifact">borkdude.gh-release-artifact</a>






## <a name="borkdude.gh-release-artifact/default-mime-types">`default-mime-types`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L6-L8)
<a name="borkdude.gh-release-artifact/default-mime-types"></a>

A map of file extensions to mime-types.

## <a name="borkdude.gh-release-artifact/release-artifact">`release-artifact`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L13-L41)
<a name="borkdude.gh-release-artifact/release-artifact"></a>
``` clojure

(release-artifact {:keys [overwrite], :or {overwrite false}, :as opts})
```


Uploads artifact to github release. Creates (draft) release if there
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
  * `:content-type` - The file's content type. Default to lookup by extension in [`default-mime-types`](#borkdude.gh-release-artifact/default-mime-types).
  * `:retries` - Attempts per request, retrying a 5xx status. Defaults to `3`.
  * `:retry-pause-ms` - Pause before the second attempt, growing with each
    one. Defaults to `1000`.

  Throws when Github does not accept the upload. Replacing an asset keeps
  the existing one until the new bytes are there.
