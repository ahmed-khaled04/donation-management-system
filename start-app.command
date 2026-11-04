#!/bin/sh
# Double-click this file on macOS to start Impact Hub.
set -eu
project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$project_dir"

# Finder does not load the terminal's Homebrew PATH.
if [ -z "${JAVA_HOME:-}" ]; then
  for java_candidate in \
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home \
    /usr/local/opt/openjdk/libexec/openjdk.jdk/Contents/Home; do
    if [ -x "$java_candidate/bin/javac" ]; then
      JAVA_HOME=$java_candidate
      export JAVA_HOME
      break
    fi
  done
fi
if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
  JAVA_HOME=$(/usr/libexec/java_home -v '21+' 2>/dev/null || true)
  export JAVA_HOME
fi
if [ -n "${JAVA_HOME:-}" ]; then
  PATH="$JAVA_HOME/bin:$PATH"
  export PATH
fi
if ! command -v javac >/dev/null 2>&1; then
  printf '%s\n' 'Impact Hub requires a JDK, version 21 or newer.'
  exit 1
fi
printf '%s\n' 'Starting Impact Hub. Keep this terminal open while the application is running.'
exec ./mvnw -ntp javafx:run
