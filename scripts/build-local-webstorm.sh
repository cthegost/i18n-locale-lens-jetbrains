#!/usr/bin/env sh

set -eu

PROJECT_ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
WEBSTORM_HOME="${WEBSTORM_HOME:-/Applications/WebStorm.app/Contents}"
VERSION="0.1.0"
PLUGIN_NAME="i18n-locale-lens"
BUILD_DIR="$PROJECT_ROOT/build/manual"
DIST_DIR="$PROJECT_ROOT/build/distributions"

if [ ! -d "$WEBSTORM_HOME" ]; then
  echo "WebStorm SDK not found: $WEBSTORM_HOME" >&2
  echo "Set WEBSTORM_HOME=/path/to/WebStorm.app/Contents and retry." >&2
  exit 1
fi

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes" "$DIST_DIR"

CLASSPATH=""
for jar in "$WEBSTORM_HOME"/lib/*.jar "$WEBSTORM_HOME"/plugins/*/lib/*.jar; do
  CLASSPATH="${CLASSPATH:+$CLASSPATH:}$jar"
done

javac -source 17 -target 17 -cp "$CLASSPATH" -d "$BUILD_DIR/classes" \
  $(find "$PROJECT_ROOT/src/main/java" -name "*.java")

cp -R "$PROJECT_ROOT/src/main/resources/"* "$BUILD_DIR/classes/"

(cd "$BUILD_DIR/classes" && jar cf "../$PLUGIN_NAME.jar" .)

mkdir -p "$BUILD_DIR/package/$PLUGIN_NAME/lib"
cp "$BUILD_DIR/$PLUGIN_NAME.jar" "$BUILD_DIR/package/$PLUGIN_NAME/lib/$PLUGIN_NAME.jar"

(cd "$BUILD_DIR/package" && zip -qr "$DIST_DIR/$PLUGIN_NAME-jetbrains-$VERSION.zip" "$PLUGIN_NAME")

echo "$DIST_DIR/$PLUGIN_NAME-jetbrains-$VERSION.zip"
