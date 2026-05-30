#!/usr/bin/env bash
#
# repatch_sonyliv.sh
# Re-patch a SonyLIV Android TV .apkm with the Chiggi patch set and sign it,
# producing a single installable APK (same settings as the current release).
#
# Usage:
#   ./scripts/repatch_sonyliv.sh [path/to/app.apkm]
#
# If no path is given, you are prompted to pick an .apkm.
#
# Overridable via environment variables:
#   APP_NAME       (default "Sony Liv Chiggi")     -> launcher name
#   PACKAGE_NAME   (default "com.sonyliv.chiggi")  -> new package id
#   KEYSTORE       (default ./Morphe.keystore)     -> signing keystore
#   MORPHE_CLI     (default ~/tools/morphe-cli/morphe-cli.jar)
#   JAVA_HOME      (default /usr/lib/jvm/java-21-openjdk-amd64)
#   ANDROID_HOME   (default ~/Android/Sdk)          -> only used for verify
#   OUT_DIR        (default: same folder as the .apkm)
#
set -euo pipefail

# ---------- Config ----------
APP_NAME="${APP_NAME:-Sony Liv Chiggi}"
PACKAGE_NAME="${PACKAGE_NAME:-com.sonyliv.chiggi}"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYSTORE="${KEYSTORE:-$PROJECT_DIR/Morphe.keystore}"
CLI="${MORPHE_CLI:-$HOME/tools/morphe-cli/morphe-cli.jar}"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$JAVA_HOME/bin:$PATH"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

die() { echo "ERROR: $*" >&2; exit 1; }

# ---------- Pre-flight checks ----------
command -v java >/dev/null 2>&1 || die "java not found. Set JAVA_HOME."
[[ -f "$CLI" ]]      || die "morphe-cli jar not found at: $CLI"
[[ -f "$KEYSTORE" ]] || die "keystore not found at: $KEYSTORE"

# ---------- Locate the .mpp patch bundle ----------
# Prefer a locally built bundle; otherwise download the latest GitHub release asset.
MPP="$(ls -t "$PROJECT_DIR"/patches/build/libs/patches-*.mpp 2>/dev/null \
        | grep -vE 'sources|javadoc' | head -1 || true)"
if [[ -z "${MPP:-}" ]]; then
  echo "No local .mpp found. Downloading the latest release bundle..."
  MPP="$TMP/patches.mpp"
  url="$(curl -fsSL https://api.github.com/repos/durgesh0505/chiggi_morphe_patches/releases/latest \
        | python3 -c "import sys,json;print(next(a['browser_download_url'] for a in json.load(sys.stdin)['assets'] if a['name'].endswith('.mpp')))")"
  curl -fsSL -o "$MPP" "$url"
fi
echo "Using patch bundle: $MPP"

# ---------- Select the .apkm ----------
APKM="${1:-}"
if [[ -z "$APKM" ]]; then
  mapfile -t found < <(find "$PROJECT_DIR" -maxdepth 3 -iname '*.apkm' 2>/dev/null)
  if [[ ${#found[@]} -gt 0 ]]; then
    echo "Select the SonyLIV Android TV .apkm to patch:"
    select choice in "${found[@]}" "Enter a path manually"; do
      if [[ "$choice" == "Enter a path manually" ]]; then
        read -e -r -p "Path to .apkm: " APKM; break
      elif [[ -n "${choice:-}" ]]; then
        APKM="$choice"; break
      fi
    done
  else
    read -e -r -p "Path to .apkm: " APKM
  fi
fi
[[ -f "$APKM" ]] || die "apkm not found: $APKM"

OUT_DIR="${OUT_DIR:-$(dirname "$APKM")}"
base="$(basename "$APKM")"; base="${base%.*}"
OUT="$OUT_DIR/${base}_chiggi_patched.apk"

# ---------- Build the options file (enable rename patches with values) ----------
OPTS="$TMP/options.json"
java -jar "$CLI" options-create -p "$MPP" -o "$OPTS" -t "$TMP/oc" >/dev/null 2>&1
APP_NAME="$APP_NAME" PACKAGE_NAME="$PACKAGE_NAME" python3 - "$OPTS" <<'PY'
import json, os, sys
path = sys.argv[1]
data = json.load(open(path))
patches = data[0]["patches"]
if "Change app name" in patches:
    patches["Change app name"]["enabled"] = True
    patches["Change app name"].setdefault("options", {})["appName"] = os.environ["APP_NAME"]
if "Change package name" in patches:
    patches["Change package name"]["enabled"] = True
    patches["Change package name"].setdefault("options", {})["packageName"] = os.environ["PACKAGE_NAME"]
json.dump(data, open(path, "w"), indent=1)
PY

# ---------- Patch + sign ----------
echo
echo "Patching '$APKM'"
echo "  app name : $APP_NAME"
echo "  package  : $PACKAGE_NAME"
echo "  keystore : $KEYSTORE"
echo
java -jar "$CLI" patch -p "$MPP" --options-file "$OPTS" --keystore "$KEYSTORE" \
  -o "$OUT" --purge -t "$TMP/patch" "$APKM"

echo
echo "✅ Patched APK: $OUT"

# ---------- Verify (best effort) ----------
AAPT="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ -x "$AAPT" ]]; then
  echo "--- verify ---"
  "$AAPT" dump badging "$OUT" 2>/dev/null \
    | grep -iE 'package: name|application-label:|leanback-launchable|native-code' | head
fi
echo
echo "Install on Google TV:  adb install -r \"$OUT\""
