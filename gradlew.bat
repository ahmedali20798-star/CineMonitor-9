@echo off
setlocal enabledelayedexpansion

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
set WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    if not exist "%APP_HOME%gradle\wrapper" mkdir "%APP_HOME%gradle\wrapper"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
)

java -Xmx64m -Xms64m -Dorg.gradle.appname="gradlew" -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
