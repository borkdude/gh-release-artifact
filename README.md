# gh-release-artifact

A babashka and Clojure lib to deploy artifacts to Github releases.
For this to work you need to set an environment variable named
`GITHUB_TOKEN` with your personal access token. You can create 
the token on github.com under your profile:
Settings -> Developer settings -> Personal access tokens.

See [API.md](API.md) for the API.

Use within babashka as follows. Add to `deps.edn` or `bb.edn`:

```
{:deps {io.github.borkdude/gh-release-artifact {:git/sha "05f8d8659e6805d513c59447ff41dc8497878462"}}}
```

Then in your code:

``` clojure
(require '[borkdude.gh-release-artifact :as ghr])

(ghr/release-artifact {:org "borkdude"
                       :repo "test-repo"
                       :tag "v0.0.15"
                       :commit "8495a6b872637ea31879c5d56160b8d8e94c9d1c"
                       :file "README.md"
                       :sha256 true
                       :overwrite true})
```

## License

Copyright © 2021 - 2022 Michiel Borkent

Distributed under the MIT License. See LICENSE.
