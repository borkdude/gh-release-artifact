# Changelog

[gh-release-artifact](https://github.com/borkdude/gh-release-artifact): Upload artifacts to Github releases idempotently

## Unreleased

- Throw when Github does not accept an upload, instead of printing the status
  and returning. A failed upload no longer leaves a green build.
- Keep an existing asset until its replacement is uploaded. A failed upload
  used to leave the release without the asset, because the old one was
  deleted first.
- Retry a 5xx status, three attempts by default. `:retries` and
  `:retry-pause-ms` set this.

## v0.2.1

- Fix binary file uploads by upgrading http-client

## v0.2.0

- Replace `babashka.curl` with `babashka.http-client`
- Bump `babashka.fs`

## v0.1.0

Initial release
