# Building

First read [BUILDING.md in Open Grind repository](https://git.opengrind.org/open-grind/open-grind/src/branch/main/BUILDING.md) for basics.

The APK has no native libraries, so one universal APK runs on every device.

## Prerequisites

Clone repository:

```bash
git clone https://git.opengrind.org/open-grind/recaptcha-helper.git
```

## Build with Docker (easiest, Linux x86_64 only)

```bash
docker compose build
docker compose run --rm build
# -> app/build/outputs/apk/release/app-release-unsigned.apk
```

### Clean up Docker

```bash
docker compose down -v
```

## Build with Nix (builds everywhere)

```bash
nix run .#build-android
# -> app/build/outputs/apk/release/app-release-unsigned.apk
```

## Build manually (advanced)

Prerequisites:

- JDK 17
- Android SDK: platform 36 and build-tools 36.0.0

`./gradlew` pins Gradle 9.4.1, so you don't need Gradle installed.

```bash
./gradlew :app:assembleRelease
# -> app/build/outputs/apk/release/app-release-unsigned.apk
```

Unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Signing

Must be the same keystore used for Open Grind releases, so that Open Grind recognizes the helper by its signature. Create it with the [keytool recipe](https://git.opengrind.org/open-grind/open-grind/src/branch/main/BUILDING.md#sign-android-build), then copy [contrib/keystore.properties.example](./contrib/keystore.properties.example).

```bash
RECAPTCHA_HELPER_KEYSTORE_PROPERTIES=/home/you/.config/open-grind/keystore.properties \
  nix run .#build-android
```

## Publishing a release

Tag `vX.Y.Z` matching `versionName` in `app/build.gradle.kts`, then run inside `nix develop`:

```bash
contrib/sign-release.sh vX.Y.Z [artifacts-dir]
# -> app/build/outputs/release/vX.Y.Z/open-grind-recaptcha-helper-vX.Y.Z-android.apk (+ .minisig)
```

Upload every file from that directory to the release.

## Verifying a release

Follow [Open Grind's § Verify minisign signature](https://git.opengrind.org/open-grind/open-grind/src/branch/main/BUILDING.md#verify-minisign-signature).

## Reproducibility

Follow [Open Grind's REPRODUCIBILITY.md](https://git.opengrind.org/open-grind/open-grind/src/branch/main/REPRODUCIBILITY.md).

| Component                                              | Pinned in                                  |
| ------------------------------------------------------ | ------------------------------------------ |
| Gradle distribution (+ SHA-256)                        | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin, reCAPTCHA Enterprise SDK, JUnit | `gradle/libs.versions.toml`                |
| compileSdk / minSdk / targetSdk, build-tools           | `app/build.gradle.kts`                     |
| R8 keep rules                                          | `app/proguard-rules.pro`                   |
| JDK + Android SDK (Nix build)                          | `flake.nix`                                |
| nixpkgs revision                                       | `flake.lock`                               |
