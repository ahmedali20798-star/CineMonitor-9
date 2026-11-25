#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [ ! -f "app/build.gradle.kts" ]; then
    echo "[ERROR] Extract the ZIP first and run push_to_github.sh from inside CanonMultiCamPro-v3.0.0/"
    exit 1
fi

if ! command -v git >/dev/null 2>&1; then
    echo "[ERROR] git is not installed."
    exit 1
fi

read -r -p "Paste your GitHub Repository URL (e.g. https://github.com/username/CanonMultiCamPro.git): " REPO_URL
REPO_URL="${REPO_URL//\"/}"
if [ -z "$REPO_URL" ]; then
    echo "[ERROR] Repository URL cannot be empty!"
    exit 1
fi

git init
git config user.name >/dev/null 2>&1 || git config user.name "Ahmed Mahmoud"
git config user.email >/dev/null 2>&1 || git config user.email "dev@canonmulticam.local"
git branch -M main
git remote remove origin 2>/dev/null || true
git remote add origin "$REPO_URL"

git add -A
git add -f .github/workflows/build_apk.yml
git add -f .gitignore
git add -f gradlew gradlew.bat gradle/wrapper/gradle-wrapper.properties
git update-index --chmod=+x gradlew push_to_github.sh

git diff --cached --quiet || git commit -m "CanonMultiCamPro v3.0.0 - Dark Studio Pro & CI/CD Ready"
git push -u origin main --force

echo "SUCCESS! Open GitHub -> Actions tab -> Build CanonMultiCamPro APK to download your APK."
