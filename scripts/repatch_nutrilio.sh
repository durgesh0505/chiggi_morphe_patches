#!/usr/bin/env bash
#
# repatch_nutrilio.sh
# Re-patch a Nutrilio split-APK bundle with the Nutrilio patch set and sign it,
# producing a single installable APK.
#
# Usage:
#   ./scripts/repatch_nutrilio.sh [net.nutrilio.zip | path/to/bundle.(zip|apks|apkm)]
#
# Input  : a bare file name is resolved against  APK/  then  nutrilio/ ; a full path
#          also works. A ".zip" split bundle is accepted and copied to ".apks" so
#          morphe-cli merges the splits. If omitted, you are prompted.
# Output : nutrilio/output/<name>_morphe_patched.apk
#
# Patches applied: Unlock Plus, Disable analytics (default on) + Change app name,
#                  Change package name, Add food search bar (enabled by this script).
#
# Overridable via environment variables:
#   APP_NAME       (default "Nutrilio Morphe")    -> launcher name
#   PACKAGE_NAME   (default "net.nutrilio.morphe") -> new package id
#   SEARCH_BAR     (default "1"; set 0 to skip)    -> Add food search bar
#   KEYSTORE       (default ./Morphe.keystore)     -> signing keystore
#   MORPHE_CLI     (default ~/tools/morphe-cli/morphe-cli.jar)
#   JAVA_HOME      (default /usr/lib/jvm/java-21-openjdk-amd64)
#   ANDROID_HOME   (default ~/Android/Sdk)          -> only used for verify
#   OUT_DIR        (default: nutrilio/output)
#
set -euo pipefail

# ---------- Config ----------
APP_NAME="${APP_NAME:-Nutrilio Morphe}"
PACKAGE_NAME="${PACKAGE_NAME:-net.nutrilio.morphe}"
SEARCH_BAR="${SEARCH_BAR:-1}"
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

# ---------- Select the input bundle ----------
SEARCH_DIRS=("$PROJECT_DIR/APK" "$PROJECT_DIR/nutrilio")

INPUT="${1:-}"
if [[ -z "$INPUT" ]]; then
  read -e -r -p "Enter bundle file name (in APK/ or nutrilio/) or full path: " INPUT
fi
if [[ ! -f "$INPUT" ]]; then
  for d in "${SEARCH_DIRS[@]}"; do
    if [[ -f "$d/$INPUT" ]]; then INPUT="$d/$INPUT"; break; fi
  done
fi
[[ -f "$INPUT" ]] || die "bundle not found: $INPUT (looked in: ${SEARCH_DIRS[*]})"

# morphe-cli rejects a ".zip" extension; copy a split .zip to .apks so it merges the splits.
base="$(basename "$INPUT")"; base="${base%.*}"
case "$INPUT" in
  *.zip) APKS="$TMP/$base.apks"; cp "$INPUT" "$APKS";;
  *)     APKS="$INPUT";;
esac

OUT_DIR="${OUT_DIR:-$PROJECT_DIR/nutrilio/output}"
mkdir -p "$OUT_DIR"
OUT="$OUT_DIR/${base}_morphe_patched.apk"

# ---------- Build the options file (enable rename + search, set values) ----------
OPTS="$TMP/options.json"
java -jar "$CLI" options-create -p "$MPP" -o "$OPTS" -t "$TMP/oc" >/dev/null 2>&1
APP_NAME="$APP_NAME" PACKAGE_NAME="$PACKAGE_NAME" SEARCH_BAR="$SEARCH_BAR" python3 - "$OPTS" <<'PY'
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
if os.environ.get("SEARCH_BAR") == "1" and "Add food search bar" in patches:
    patches["Add food search bar"]["enabled"] = True
json.dump(data, open(path, "w"), indent=1)
PY

# ---------- Patch + sign ----------
echo
echo "Patching '$INPUT'"
echo "  app name   : $APP_NAME"
echo "  package    : $PACKAGE_NAME"
echo "  search bar : $([[ "$SEARCH_BAR" == "1" ]] && echo on || echo off)"
echo "  keystore   : $KEYSTORE"
echo
java -jar "$CLI" patch -p "$MPP" --options-file "$OPTS" --keystore "$KEYSTORE" \
  -o "$OUT" --purge -t "$TMP/patch" "$APKS"

# Remove the split-merge intermediate morphe-cli drops in the output dir.
rm -f "$OUT_DIR"/*-merged.apk 2>/dev/null || true

echo
echo "✅ Patched APK: $OUT"

# ---------- Verify (best effort) ----------
AAPT="$ANDROID_HOME/build-tools/36.0.0/aapt2"
if [[ -x "$AAPT" ]]; then
  echo "--- verify ---"
  "$AAPT" dump badging "$OUT" 2>/dev/null \
    | grep -iE 'package: name|application-label:|launchable-activity' | head
fi
echo
echo "Install on phone:  adb install -r \"$OUT\""
