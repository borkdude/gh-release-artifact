# Table of contents
-  [`borkdude.gh-release-artifact`](#borkdude.gh-release-artifact) 
    -  [`default-mime-types`](#borkdude.gh-release-artifact/default-mime-types) - A map of file extensions to mime-types.
    -  [`overwrite-asset`](#borkdude.gh-release-artifact/overwrite-asset)
    -  [`upload-asset`](#borkdude.gh-release-artifact/upload-asset)
-  [`borkdude.gh-release-artifact.internal`](#borkdude.gh-release-artifact.internal) 
    -  [`-release-for`](#borkdude.gh-release-artifact.internal/-release-for)
    -  [`create-release`](#borkdude.gh-release-artifact.internal/create-release)
    -  [`current-commit`](#borkdude.gh-release-artifact.internal/current-commit)
    -  [`default-mime-types`](#borkdude.gh-release-artifact.internal/default-mime-types) - A map of file extensions to mime-types.
    -  [`delete-release`](#borkdude.gh-release-artifact.internal/delete-release)
    -  [`endpoint`](#borkdude.gh-release-artifact.internal/endpoint)
    -  [`get-draft-release`](#borkdude.gh-release-artifact.internal/get-draft-release)
    -  [`list-assets`](#borkdude.gh-release-artifact.internal/list-assets)
    -  [`list-releases`](#borkdude.gh-release-artifact.internal/list-releases)
    -  [`overwrite-asset`](#borkdude.gh-release-artifact.internal/overwrite-asset)
    -  [`path`](#borkdude.gh-release-artifact.internal/path)
    -  [`release-endpoint`](#borkdude.gh-release-artifact.internal/release-endpoint)
    -  [`release-for`](#borkdude.gh-release-artifact.internal/release-for)
    -  [`token`](#borkdude.gh-release-artifact.internal/token)
    -  [`with-gh-headers`](#borkdude.gh-release-artifact.internal/with-gh-headers)

-----
# <a name="borkdude.gh-release-artifact">borkdude.gh-release-artifact</a>






## <a name="borkdude.gh-release-artifact/default-mime-types">`default-mime-types`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L12-L107)
<a name="borkdude.gh-release-artifact/default-mime-types"></a>

A map of file extensions to mime-types.

## <a name="borkdude.gh-release-artifact/overwrite-asset">`overwrite-asset`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L109-L110)
<a name="borkdude.gh-release-artifact/overwrite-asset"></a>
``` clojure

(overwrite-asset opts)
```


## <a name="borkdude.gh-release-artifact/upload-asset">`upload-asset`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L112-L113)
<a name="borkdude.gh-release-artifact/upload-asset"></a>
``` clojure

(upload-asset opts)
```


-----
# <a name="borkdude.gh-release-artifact.internal">borkdude.gh-release-artifact.internal</a>






## <a name="borkdude.gh-release-artifact.internal/-release-for">`-release-for`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L62-L77)
<a name="borkdude.gh-release-artifact.internal/-release-for"></a>
``` clojure

(-release-for {:keys [:org :repo :tag], :as opts})
```


## <a name="borkdude.gh-release-artifact.internal/create-release">`create-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L41-L57)
<a name="borkdude.gh-release-artifact.internal/create-release"></a>
``` clojure

(create-release
 {:keys [:tag :commit :org :repo :draft :target-commitish :prerelease],
  :or {draft true, target-commitish (or commit (current-commit))}})
```


## <a name="borkdude.gh-release-artifact.internal/current-commit">`current-commit`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L36-L39)
<a name="borkdude.gh-release-artifact.internal/current-commit"></a>
``` clojure

(current-commit)
```


## <a name="borkdude.gh-release-artifact.internal/default-mime-types">`default-mime-types`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L88-L183)
<a name="borkdude.gh-release-artifact.internal/default-mime-types"></a>

A map of file extensions to mime-types.

## <a name="borkdude.gh-release-artifact.internal/delete-release">`delete-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L59-L60)
<a name="borkdude.gh-release-artifact.internal/delete-release"></a>
``` clojure

(delete-release {:keys [:org :repo :id]})
```


## <a name="borkdude.gh-release-artifact.internal/endpoint">`endpoint`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L12-L12)
<a name="borkdude.gh-release-artifact.internal/endpoint"></a>

## <a name="borkdude.gh-release-artifact.internal/get-draft-release">`get-draft-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L31-L34)
<a name="borkdude.gh-release-artifact.internal/get-draft-release"></a>
``` clojure

(get-draft-release org repo tag)
```


## <a name="borkdude.gh-release-artifact.internal/list-assets">`list-assets`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L81-L85)
<a name="borkdude.gh-release-artifact.internal/list-assets"></a>
``` clojure

(list-assets opts)
```


## <a name="borkdude.gh-release-artifact.internal/list-releases">`list-releases`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L25-L29)
<a name="borkdude.gh-release-artifact.internal/list-releases"></a>
``` clojure

(list-releases org repo)
```


## <a name="borkdude.gh-release-artifact.internal/overwrite-asset">`overwrite-asset`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L185-L227)
<a name="borkdude.gh-release-artifact.internal/overwrite-asset"></a>
``` clojure

(overwrite-asset {:keys [:file :content-type], :as opts})
```


## <a name="borkdude.gh-release-artifact.internal/path">`path`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L14-L15)
<a name="borkdude.gh-release-artifact.internal/path"></a>
``` clojure

(path & strs)
```


## <a name="borkdude.gh-release-artifact.internal/release-endpoint">`release-endpoint`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L17-L18)
<a name="borkdude.gh-release-artifact.internal/release-endpoint"></a>
``` clojure

(release-endpoint org repo)
```


## <a name="borkdude.gh-release-artifact.internal/release-for">`release-for`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L79-L79)
<a name="borkdude.gh-release-artifact.internal/release-for"></a>

## <a name="borkdude.gh-release-artifact.internal/token">`token`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L10-L10)
<a name="borkdude.gh-release-artifact.internal/token"></a>
``` clojure

(token)
```


## <a name="borkdude.gh-release-artifact.internal/with-gh-headers">`with-gh-headers`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact/internal.clj#L20-L23)
<a name="borkdude.gh-release-artifact.internal/with-gh-headers"></a>
``` clojure

(with-gh-headers m)
```

