# Open Grind reCAPTCHA helper

An Android helper add-on for [Open Grind](https://git.opengrind.org/open-grind/open-grind) that mints reCAPTCHA Enterprise tokens on demand for Grindr's API.

Requires Android >= 9 and Google Play services. Tokens minted on Android emulators are usually rejected.

The add-on depends on Google's proprietary [reCAPTCHA Enterprise SDK](https://cloud.google.com/recaptcha/docs/instrument-android-apps) (`com.google.android.recaptcha:recaptcha`).

## Building

See [BUILDING.md](./BUILDING.md).

Open Grind reCAPTCHA helper supports reproducible builds. Read more in [BUILDING.md § Reproducibility](./BUILDING.md#reproducibility).

## Security

Releases are signed with [minisign](https://jedisct1.github.io/minisign/) and ship a detached `.minisig` — see [how to verify](https://git.opengrind.org/open-grind/open-grind/src/branch/main/BUILDING.md#verify-minisign-signature). Never install from unofficial sources.

## License

[MIT](./LICENSE)
