#!/usr/bin/env sh
set -eu

APP_HOME="$(cd "$(dirname "$0")" && pwd -P)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar"

if [ ! -s "$WRAPPER_JAR" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    if command -v curl >/dev/null 2>&1; then
        curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -qO "$WRAPPER_JAR" "$WRAPPER_URL"
    elif command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "ERROR: Neither gradle-wrapper.jar, curl, wget, nor gradle was found." >&2
        exit 1
    fi
fi

exec java -Xmx64m -Xms64m \
    -Dorg.gradle.appname="gradlew" \
    -classpath "$WRAPPER_JAR" \
    org.gradle.wrapper.GradleWrapperMain "$@"
