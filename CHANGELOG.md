# Changelog

[gh-release-artifact](https://github.com/borkdude/gh-release-artifact): Upload artifacts to Github releases idempotently

## Unreleased

- Throw when Github does not accept an upload, instead of printing the status
  and returning. A failed upload no longer leaves a green build.
- Keep an asset of the name on the release throughout a replacement: the new
  one once the upload is through, the existing one otherwise. A failed upload
  used to leave the release without the asset, because the old one was
  deleted first.
- Retry a server error and a rate limit, three attempts by default, honouring
  `Retry-After`. `:retries` and `:retry-pause-ms` set this. An attempt that
  Github answered with an error can still have created the asset, so the next
  attempt clears it first, and so does giving up.

## v0.2.1

- Fix binary file uploads by upgrading http-client

## v0.2.0

- Replace `babashka.curl` with `babashka.http-client`
- Bump `babashka.fs`

## v0.1.0

Initial release
