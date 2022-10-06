# Table of contents
-  [`borkdude.gh-release-artifact`](#borkdude.gh-release-artifact) 
    -  [`-release-for`](#borkdude.gh-release-artifact/-release-for)
    -  [`create-release`](#borkdude.gh-release-artifact/create-release)
    -  [`current-commit`](#borkdude.gh-release-artifact/current-commit)
    -  [`default-mime-types`](#borkdude.gh-release-artifact/default-mime-types) - A map of file extensions to mime-types.
    -  [`delete-release`](#borkdude.gh-release-artifact/delete-release)
    -  [`endpoint`](#borkdude.gh-release-artifact/endpoint)
    -  [`get-draft-release`](#borkdude.gh-release-artifact/get-draft-release)
    -  [`list-assets`](#borkdude.gh-release-artifact/list-assets)
    -  [`list-releases`](#borkdude.gh-release-artifact/list-releases)
    -  [`overwrite-asset`](#borkdude.gh-release-artifact/overwrite-asset)
    -  [`path`](#borkdude.gh-release-artifact/path)
    -  [`release-endpoint`](#borkdude.gh-release-artifact/release-endpoint)
    -  [`release-for`](#borkdude.gh-release-artifact/release-for)
    -  [`token`](#borkdude.gh-release-artifact/token)
    -  [`with-gh-headers`](#borkdude.gh-release-artifact/with-gh-headers)

-----
# <a name="borkdude.gh-release-artifact">borkdude.gh-release-artifact</a>






## <a name="borkdude.gh-release-artifact/-release-for">`-release-for`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L63-L78)
<a name="borkdude.gh-release-artifact/-release-for"></a>
``` clojure

(-release-for {:keys [:org :repo :tag], :as opts})
```


## <a name="borkdude.gh-release-artifact/create-release">`create-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L42-L58)
<a name="borkdude.gh-release-artifact/create-release"></a>
``` clojure

(create-release
 {:keys [:tag :commit :org :repo :draft :target-commitish :prerelease],
  :or {draft true, target-commitish (or commit (current-commit))}})
```


## <a name="borkdude.gh-release-artifact/current-commit">`current-commit`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L37-L40)
<a name="borkdude.gh-release-artifact/current-commit"></a>
``` clojure

(current-commit)
```


## <a name="borkdude.gh-release-artifact/default-mime-types">`default-mime-types`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L89-L184)
<a name="borkdude.gh-release-artifact/default-mime-types"></a>

A map of file extensions to mime-types.

## <a name="borkdude.gh-release-artifact/delete-release">`delete-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L60-L61)
<a name="borkdude.gh-release-artifact/delete-release"></a>
``` clojure

(delete-release {:keys [:org :repo :id]})
```


## <a name="borkdude.gh-release-artifact/endpoint">`endpoint`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L13-L13)
<a name="borkdude.gh-release-artifact/endpoint"></a>

## <a name="borkdude.gh-release-artifact/get-draft-release">`get-draft-release`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L32-L35)
<a name="borkdude.gh-release-artifact/get-draft-release"></a>
``` clojure

(get-draft-release org repo tag)
```


## <a name="borkdude.gh-release-artifact/list-assets">`list-assets`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L82-L86)
<a name="borkdude.gh-release-artifact/list-assets"></a>
``` clojure

(list-assets opts)
```


## <a name="borkdude.gh-release-artifact/list-releases">`list-releases`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L26-L30)
<a name="borkdude.gh-release-artifact/list-releases"></a>
``` clojure

(list-releases org repo)
```


## <a name="borkdude.gh-release-artifact/overwrite-asset">`overwrite-asset`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L186-L228)
<a name="borkdude.gh-release-artifact/overwrite-asset"></a>
``` clojure

(overwrite-asset {:keys [:file :content-type], :as opts})
```


## <a name="borkdude.gh-release-artifact/path">`path`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L15-L16)
<a name="borkdude.gh-release-artifact/path"></a>
``` clojure

(path & strs)
```


## <a name="borkdude.gh-release-artifact/release-endpoint">`release-endpoint`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L18-L19)
<a name="borkdude.gh-release-artifact/release-endpoint"></a>
``` clojure

(release-endpoint org repo)
```


## <a name="borkdude.gh-release-artifact/release-for">`release-for`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L80-L80)
<a name="borkdude.gh-release-artifact/release-for"></a>

## <a name="borkdude.gh-release-artifact/token">`token`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L11-L11)
<a name="borkdude.gh-release-artifact/token"></a>

## <a name="borkdude.gh-release-artifact/with-gh-headers">`with-gh-headers`</a> [:page_facing_up:](https://github.com/borkdude/gh-release-artifact/blob/main/src/borkdude/gh_release_artifact.clj#L21-L24)
<a name="borkdude.gh-release-artifact/with-gh-headers"></a>
``` clojure

(with-gh-headers m)
```

