{
  description = "Open Grind reCAPTCHA helper Android add-on — declarative build toolchain";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachSystem
      [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ]
      (
        system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config = {
              android_sdk.accept_license = true;
              allowUnfree = true;
            };
          };

          androidPlatformVersion = "36";
          androidBuildToolsVersion = "36.0.0";

          androidComposition = pkgs.androidenv.composeAndroidPackages {
            platformVersions = [ androidPlatformVersion ];
            buildToolsVersions = [ androidBuildToolsVersion ];
            includeNDK = false;
            includeEmulator = false;
            includeSources = false;
            includeSystemImages = false;
            includeExtras = [ ];
          };

          androidSdk = androidComposition.androidsdk;
          androidSdkRoot = "${androidSdk}/libexec/android-sdk";
          buildToolsBin = "${androidSdkRoot}/build-tools/${androidBuildToolsVersion}";

          jdk = pkgs.jdk17_headless;

          toolchainInputs = [
            jdk
            androidSdk
            pkgs.coreutils
            pkgs.minisign
          ];

          buildEnv = {
            JAVA_HOME = jdk.home;
            ANDROID_HOME = androidSdkRoot;
            ANDROID_SDK_ROOT = androidSdkRoot;
          };

          envExports = pkgs.lib.concatStringsSep "\n" (
            pkgs.lib.mapAttrsToList (k: v: "export ${k}=${v}") buildEnv
          );

          buildAndroidScript = pkgs.writeShellApplication {
            name = "recaptcha-helper-build-android";
            runtimeInputs = toolchainInputs;
            text = ''
              set -euo pipefail

              ${envExports}
              export PATH="${buildToolsBin}:$PATH"

              ROOT="''${RECAPTCHA_HELPER_ROOT:-$PWD}"
              cd "$ROOT"

              export GRADLE_USER_HOME="''${RECAPTCHA_HELPER_GRADLE_USER_HOME:-$HOME/.gradle-recaptcha-helper}"
              mkdir -p "$GRADLE_USER_HOME"
              printf 'android.aapt2FromMavenOverride=%s/aapt2\n' "${buildToolsBin}" > "$GRADLE_USER_HOME/gradle.properties"

              TASK="''${1:-:app:assembleRelease}"
              ./gradlew "$TASK"

              kp="''${RECAPTCHA_HELPER_KEYSTORE_PROPERTIES:-}"
              case "$kp" in "~"*) kp="$HOME''${kp#\~}" ;; esac
              if [ -n "$kp" ] && [ -f "$kp" ]; then sfx=""; else sfx="-unsigned"; fi

              echo
              case "$TASK" in
                *ebug*)
                  echo "Produced:"
                  apks=("$ROOT/app/build/outputs/apk/debug/app-debug.apk")
                  ;;
                *)
                  echo "Produced (release):"
                  apks=("$ROOT/app/build/outputs/apk/release/app-release$sfx.apk")
                  ;;
              esac
              for apk in "''${apks[@]}"; do
                if [ -f "$apk" ]; then
                  printf '  %s (%s)\n' "$apk" "$(du -h "$apk" | cut -f1)"
                else
                  printf '  %s\n' "$apk"
                fi
              done
            '';
          };
        in
        {
          devShells.default = pkgs.mkShell (
            buildEnv
            // {
              packages = toolchainInputs;
              shellHook = ''
                export PATH="${buildToolsBin}:$PATH"

                echo "Open Grind reCAPTCHA helper dev shell: Android toolchain pinned via Nix."
                echo "  JDK: $JAVA_HOME"
                echo "  SDK: $ANDROID_HOME"
              '';
            }
          );

          packages = {
            default = buildAndroidScript;
            build-android = buildAndroidScript;
          };

          apps = {
            default = {
              type = "app";
              program = "${buildAndroidScript}/bin/recaptcha-helper-build-android";
            };
            build-android = {
              type = "app";
              program = "${buildAndroidScript}/bin/recaptcha-helper-build-android";
            };
          };

          formatter = pkgs.nixfmt-rfc-style;
        }
      );
}
