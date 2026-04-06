#!/usr/bin/env bash
set -e

JAVA_HOME_WIN="C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.2.10-hotspot"
JAVA_HOME_UNIX="/c/Program Files/Eclipse Adoptium/jdk-25.0.2.10-hotspot"
JAVAC="$JAVA_HOME_UNIX/bin/javac"
JAR_TOOL="$JAVA_HOME_UNIX/bin/jar"

PROJECT="/c/Users/yezza/Desktop/orient/H26-IFT-GLO-Equipe5"
PROJECT_WIN=$(cygpath -w "$PROJECT")
SRC="$PROJECT/SRC/main/java"
LIB="$PROJECT/lib"
BUILD="$PROJECT/build_tmp"

echo "=== 1. Nettoyage ==="
rm -rf "$BUILD"
mkdir -p "$BUILD/classes" "$BUILD/extracted"

echo "=== 2. Extraction des dependances ==="
for JAR in "$LIB"/*.jar; do
    echo "  Extraction : $(basename $JAR)"
    (cd "$BUILD/extracted" && "$JAR_TOOL" xf "$JAR")
done
rm -f "$BUILD/extracted/META-INF/"*.SF \
      "$BUILD/extracted/META-INF/"*.DSA \
      "$BUILD/extracted/META-INF/"*.RSA

echo "=== 3. Collecte des sources .java (format Windows) ==="
find "$SRC" -name "*.java" | while read f; do
    cygpath -w "$f"
done > "$BUILD/sources.txt"
echo "  $(wc -l < "$BUILD/sources.txt") fichiers trouves"

echo "=== 4. Compilation Java 25 ==="
CP="${PROJECT_WIN}\\lib\\pdfbox-3.0.3.jar;${PROJECT_WIN}\\lib\\fontbox-3.0.3.jar;${PROJECT_WIN}\\lib\\pdfbox-io-3.0.3.jar;${PROJECT_WIN}\\lib\\commons-logging-1.3.4.jar"
"$JAVAC" \
    --release 25 \
    -encoding UTF-8 \
    -cp "$CP" \
    -d "$(cygpath -w "$BUILD/classes")" \
    @"$(cygpath -w "$BUILD/sources.txt")"
echo "  Compilation OK"

echo "=== 5. Fusion classes + dependances ==="
cp -r "$BUILD/extracted/"* "$BUILD/classes/" 2>/dev/null || true

echo "=== 6. Creation du MANIFEST ==="
mkdir -p "$BUILD/classes/META-INF"
printf 'Manifest-Version: 1.0\r\nMain-Class: vue.MainWindow\r\n\r\n' \
    > "$BUILD/classes/META-INF/MANIFEST.MF"

echo "=== 7. Creation du fat JAR ==="
(cd "$BUILD/classes" && "$JAR_TOOL" cfm \
    "$(cygpath -w "$PROJECT/equipe05.jar")" \
    META-INF/MANIFEST.MF .)

echo ""
echo "=== SUCCES ==="
SIZE=$(wc -c < "$PROJECT/equipe05.jar")
echo "  equipe05.jar cree : $SIZE octets"

echo ""
echo "=== Nettoyage build_tmp ==="
rm -rf "$BUILD"
