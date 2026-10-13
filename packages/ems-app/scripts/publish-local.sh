#!/usr/bin/env bash
#
# 本地开发版发布：构建 → 写入开发版号 → 发布到**本地 registry**（dist-tag `dev`）。
# 用法：pnpm publishLocal [版本号]
#   pnpm publishLocal                 # 自动版本号 X.Y.Z-dev.<UTC yyyyMMddHHmmss>
#   pnpm publishLocal 0.1.0-dev.1     # 指定版本号（等价于 npm version 0.1.0-dev.1 --no-git-tag-version）
#
# 与 release-dev.sh 的区别：只发本地 registry、不碰 npmjs、不要求 git 干净；用于同机联调。
# 前端项目（sashub 等）统一按「本地 registry 上的开发版」消费本包，不再用 `link:`。
# 需要正式的对外预览版时用 ./scripts/release-dev.sh。
#
# 可用环境变量覆盖：
#   LOCAL_REGISTRY  本地 registry，默认 http://localhost/npm/
#   BUILD_CMD       默认 pnpm build
#   TEST_CMD        默认 pnpm test；SKIP_TESTS=1 或 TEST_CMD= 可跳过
#   PUBLISH_CMD     默认 pnpm publish（已带 registry/tag/no-git-checks/ignore-scripts）
#   DRY_RUN=1       只打包 + 打印目标 registry，不真正发布（换 registry 前先跑它确认）
#
# 注意：pnpm 里 publishConfig.registry 会盖过 --registry，本脚本发布前会临时把 publishConfig.registry
#   指到 LOCAL_REGISTRY（结束复原），所以在 ems-app 这类 publishConfig 指向 npmjs 的包里也不会误发公网。
#
# 注：registry 鉴权按 host+path 匹配，~/.npmrc 里的 token 条目必须与 LOCAL_REGISTRY 一致
#   （//127.0.0.1:8080/npm/:_authToken=*** 与 //localhost/npm/:_authToken=*** 是两条，不能互用）。
#
# 版本号只在发布过程中临时写入 package.json，结束（含失败）后立刻复原，全程不做 git 操作。
# 注：同一版本号重复发布时，pnpm 的 store 可能命中旧字节；自动时间戳版本号天然唯一，建议直接用自动版本号。
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"
PKG_NAME="$(node -p "require('./package.json').name")"

LOCAL_REGISTRY="${LOCAL_REGISTRY:-http://localhost/npm/}"
case "$LOCAL_REGISTRY" in
  */) ;;
  *) LOCAL_REGISTRY="$LOCAL_REGISTRY/" ;;
esac
BUILD_CMD="${BUILD_CMD:-pnpm build}"
if [ "${SKIP_TESTS:-0}" = "1" ]; then
  TEST_CMD=""
else
  TEST_CMD="${TEST_CMD:-pnpm test}"
fi
# 已显式 build，用 --ignore-scripts 跳过 prepublishOnly 的重复构建。
PUBLISH_CMD="${PUBLISH_CMD:-pnpm publish --registry $LOCAL_REGISTRY --tag dev --no-git-checks --ignore-scripts}"
if [ "${DRY_RUN:-0}" = "1" ]; then
  PUBLISH_CMD="$PUBLISH_CMD --dry-run"
fi

log() { printf '\033[1;36m[publish-local]\033[0m %s\n' "$*"; }
die() { printf '\033[1;31m[publish-local] 错误:\033[0m %s\n' "$*" >&2; exit 1; }

BASE="$(node -p "require('./package.json').version")"
if node -e "const d=require('./package.json').dependencies||{};process.exit(Object.values(d).some(v=>/^(link|file):/.test(String(v)))?0:1)"; then
  die "package.json 的 dependencies 仍有 link:/file: 本地路径，换成正式版本号后再发布"
fi

DEV_VERSION="${1:-}"
if [ -n "$DEV_VERSION" ]; then
  log "使用指定版本号: $DEV_VERSION"
else
  case "$BASE" in
    *-*) DEV_BASE="${BASE%%-*}" ;;
    *) DEV_BASE="$BASE" ;;
  esac
  case "$DEV_BASE" in
    [0-9]*.[0-9]*.[0-9]*) ;;
    *) die "package.json 版本 $BASE 不是 X.Y.Z 形式，无法推导开发版号（可显式传入版本号）" ;;
  esac
  DEV_VERSION="$DEV_BASE-dev.$(date -u +%Y%m%d%H%M%S)"
  log "自动版本号: $DEV_VERSION（基线 $DEV_BASE，通道 dev）"
fi
case "$DEV_VERSION" in
  [0-9]*.[0-9]*.[0-9]*) ;;
  *) die "版本号 $DEV_VERSION 不是 X.Y.Z[-pre] 形式" ;;
esac

BACKUP="$(mktemp "${TMPDIR:-/tmp}/npm-package.json.XXXXXX")"
cp package.json "$BACKUP"
restore() { cp "$BACKUP" package.json; rm -f "$BACKUP"; }
trap restore EXIT

log "临时写入 package.json: $PKG_NAME@$DEV_VERSION"
# 同时把 publishConfig.registry 指到 LOCAL_REGISTRY：实测 pnpm 里 publishConfig.registry 会盖过 --registry，
# 不中和它的话「本地发布」会打到 npmjs（@beangle/ems-app 这类包在 npmjs 上是真实存在的）。
# 原本没有 publishConfig 就不新增该字段。改动随 package.json 一起在结束时复原。
node -e "
const fs = require('fs')
const p = JSON.parse(fs.readFileSync('package.json', 'utf8'))
p.version = '$DEV_VERSION'
if (p.publishConfig) p.publishConfig.registry = '$LOCAL_REGISTRY'
fs.writeFileSync('package.json', JSON.stringify(p, null, 2) + '\n')
"

log "构建: $BUILD_CMD"
$BUILD_CMD
if [ -n "$TEST_CMD" ]; then
  log "测试: $TEST_CMD"
  $TEST_CMD
fi

log "发布到 $LOCAL_REGISTRY（dist-tag: dev）: $PUBLISH_CMD"
if ! $PUBLISH_CMD; then
  die "发布失败。若报 E401：registry 鉴权按 host+path 匹配，~/.npmrc 里需要与 $LOCAL_REGISTRY 完全对应的一条，例如 //127.0.0.1:8080/npm/:_authToken=***（localhost 与 127.0.0.1 视为不同 registry）；也可用 LOCAL_REGISTRY=... 指定已有 token 的地址"
fi

restore
trap - EXIT

if npm view "$PKG_NAME@$DEV_VERSION" version --registry "$LOCAL_REGISTRY" >/dev/null 2>&1; then
  log "已确认 $PKG_NAME@$DEV_VERSION 在 $LOCAL_REGISTRY 可查"
else
  log "警告：在 $LOCAL_REGISTRY 查不到 $PKG_NAME@$DEV_VERSION，请确认 registry 是否已收录"
fi
if [ "${DRY_RUN:-0}" = "1" ]; then
  log "dry-run 结束：仅打包并确认目标 registry，未真正发布；package.json 已复原为 $BASE。"
  exit 0
fi
log "已发布 $PKG_NAME@$DEV_VERSION（package.json 已复原为 $BASE）。消费方安装："
log "  pnpm add $PKG_NAME@dev --registry $LOCAL_REGISTRY"
log "  pnpm add $PKG_NAME@$DEV_VERSION --registry $LOCAL_REGISTRY"
