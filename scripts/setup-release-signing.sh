#!/usr/bin/env bash
# One-time setup of GrayDial's release signing key. Run it yourself in a terminal.
#
# - Creates the signing key OUTSIDE the repository (~/.graydial/graydial-release.jks).
# - Uploads it, base64-encoded, plus its password and alias as GitHub Actions secrets of
#   Yedidyar/graydial, so the Release workflow can sign APKs.
#
# BACK UP the .jks file and password (e.g. in a password manager). Every future update must be
# signed with this same key, or Android refuses to install it over the existing app.
set -euo pipefail

REPO="${REPO:-Yedidyar/graydial}"
KEYSTORE="${KEYSTORE:-$HOME/.graydial/graydial-release.jks}"
ALIAS="graydial"

if [ -e "$KEYSTORE" ]; then
  echo "A keystore already exists at $KEYSTORE; refusing to overwrite it." >&2
  exit 1
fi
mkdir -p "$(dirname "$KEYSTORE")"
chmod 700 "$(dirname "$KEYSTORE")"

read -rs -p "Choose a keystore password (min 6 chars): " GRAYDIAL_PW; echo
read -rs -p "Repeat the password: " GRAYDIAL_PW2; echo
[ "$GRAYDIAL_PW" = "$GRAYDIAL_PW2" ] || { echo "Passwords do not match." >&2; exit 1; }
export GRAYDIAL_PW

keytool -genkeypair -keystore "$KEYSTORE" -storetype PKCS12 -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 -validity 10000 \
  -dname "CN=GrayDial" \
  -storepass:env GRAYDIAL_PW -keypass:env GRAYDIAL_PW
chmod 600 "$KEYSTORE"
echo "Created $KEYSTORE"

GH_TOKEN="$(gh auth token -u Yedidyar)"
export GH_TOKEN
base64 < "$KEYSTORE" | tr -d '\n' | gh secret set GRAYDIAL_KEYSTORE_BASE64 -R "$REPO"
printf '%s' "$GRAYDIAL_PW" | gh secret set GRAYDIAL_STORE_PASSWORD -R "$REPO"
printf '%s' "$GRAYDIAL_PW" | gh secret set GRAYDIAL_KEY_PASSWORD -R "$REPO"
printf '%s' "$ALIAS" | gh secret set GRAYDIAL_KEY_ALIAS -R "$REPO"
unset GRAYDIAL_PW GRAYDIAL_PW2

echo "Secrets set on $REPO. Now back up $KEYSTORE and its password somewhere safe."
