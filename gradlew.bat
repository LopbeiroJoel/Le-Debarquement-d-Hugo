@echo off
cd /d "%~dp0"
java --enable-native-access=ALL-UNNAMED -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain %*
