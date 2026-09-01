#!/bin/bash
# Build native image for the beangle-ems native module.
# Usage: ./build-native.sh [mainClass] [outputName]
#   default mainClass: org.beangle.sas.engine.tomcat.Bootstrap
#   default output:    target/native/ems-native
set -e

GRAALVM_HOME="${GRAALVM_HOME:-${JAVA_HOME:-/home/chaostone/local/graalvm-jdk-21}}"
NATIVE_IMAGE="$GRAALVM_HOME/bin/native-image"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(cd "$PROJECT_DIR/.." && pwd)"
MAIN_CLASS="${1:-org.beangle.sas.engine.tomcat.Bootstrap}"
OUTPUT="${2:-$PROJECT_DIR/target/native/ems-native}"
cd "$REPO_DIR"

# Step 1: 先编译再打包刷新工程 jar（sbt 2 的 AOT 资源生成依赖上一轮编译产物，
# 若直接在 packageBin 里改源码，本轮 jar 会带上过期的 reflect-config；先 compile
# 确保 classes 就绪，再 packageBin 打包），然后取 classpath。
# sbt 2 的 fullClasspath 对工程返回 CAS 内容寻址 jar（target/out/.../*.jar 是指向
# ~/.cache/sbt/v2/cas/sha256-*/ 的符号链接），compile 只更新 classes/resource_managed，
# 不会重建 jar；若不先 packageBin，native 构建会用到旧 classes（见 data 仓库 docs/sbt2-cas-jar.md）。
sbt -batch "native/Compile/compile;native/Compile/packageBin" || exit 1
CP_FILE="$REPO_DIR/target/native/native-image-cp.txt"
mkdir -p "$(dirname "$CP_FILE")"
sbt -batch "show native/Compile/fullClasspath" 2>&1 | grep "Attributed(" | sed 's/.*Attributed(//;s/)//;s/\* //;s/^ *//' | sed 's/>.*//' | \
    sed "s|\${OUT}|$REPO_DIR/target/out|g" | \
    sed "s|\${CSR_CACHE}|$HOME/.cache/coursier/v1|g" | \
    sed "s|\${IVY_HOME}|$HOME/.ivy2|g" | \
    sed "s|/target/out/target/out/|/target/out/|g" | \
    sed "s|/target/out//|/target/out/|g" | \
    sed 's/>sha256-[a-f0-9]*\/[0-9]*//g' | \
    grep -v "^$" > "$CP_FILE"

echo "Classpath entries: $(wc -l < "$CP_FILE")"
echo "Main class: $MAIN_CLASS"

mkdir -p "$(dirname "$OUTPUT")"

# Step 2: Run native-image.
# 库侧配置（reflect/resource/initialize）由 beangle-data-hibernate.jar /
# beangle-commons.jar 内嵌的 META-INF/native-image 自动发现；ems 侧注册器按模块
# 放置（app/portal/native 各自的 aot-registrars.txt，AotPlugin 按模块生成
# META-INF/native-image/beangle 随各 jar 发布），native 构建时自动合并，无需手写文件。
"$NATIVE_IMAGE" \
    --no-fallback \
    --enable-url-protocols=jar,resource,http,https \
    -H:+AddAllCharsets \
    -H:+ReportExceptionStackTraces \
    --report-unsupported-elements-at-runtime \
    --initialize-at-build-time=ch.qos.logback,org.slf4j \
    -cp "$(cat "$CP_FILE" | tr '\n' ':')" \
    -o "$OUTPUT" \
    "$MAIN_CLASS"

echo "Native image built: $OUTPUT"
