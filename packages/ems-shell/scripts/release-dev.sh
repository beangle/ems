#!/usr/bin/env bash
#
# 开发预览版发布（git-free）：X.Y.Z-dev.<UTC yyyyMMddHHmmss> → 以 dist-tag `dev` 发布。
#
# 用法：pnpm release:dev        （或 bash scripts/release-dev.sh）
#
# 版本号规则（npm/semver，详见包内 README「发布」）：
#   - 前缀 X.Y.Z 取 package.json 里的「下一个正式版号」：`0.0.16-dev` 与 `0.0.16` 都取 0.0.16
#     （正式发布脚本 ~/bin/npm-publish.sh 会把版本号改写成下一个 X.Y.(Z+1)-dev）；
#   - `dev` 是通道名，UTC 时间戳保证每个构建的版本号唯一且递增——绝不重发同一个版本号
#     （npm 体系里 lockfile 记 integrity、store 内容寻址，同号重发只会让消费方卡在旧字节）；
#   - 发布用 `--tag dev`，`latest` 永远指向最高正式版；消费方用 `@scope/name@dev`
#     跟随最新预览，或写 `^X.Y.Z-dev.0` 这类范围。
#
# 本脚本不改动 git：版本号只在发布过程中临时写入 package.json，结束（含失败）后立刻复原，
# 备份文件放在仓库之外，全程没有 checkout / commit / push。
set -euo pipefail

PKG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PKG_DIR"
PKG_NAME="$(node -p "require('./package.json').name")"

# 发布走 npm：pnpm publish 对 npmjs 会报 PUT 404（本仓库实测）。这里**不加** --ignore-scripts：
# prepublishOnly（pnpm build && node build.mjs --clean）负责清掉 dist/ 下的版本快照目录，
# 跳过它会把历史快照一起打进包里。
BUILD_CMD="${BUILD_CMD:-pnpm build}"
TEST_CMD="${TEST_CMD:-pnpm test}"
PUBLISH_CMD="${PUBLISH_CMD:-npm publish --tag dev --access public}"

log() { printf '\033[1;36m[release]\033[0m %s\n' "$*"; }
die() { printf '\033[1;31m[release] 错误:\033[0m %s\n' "$*" >&2; exit 1; }

RAW="$(node -p "require('./package.json').version")"
BASE="${RAW%%-dev*}"
if node -e "const d=require('./package.json').dependencies||{};process.exit(Object.values(d).some(v=>/^(link|file):/.test(String(v)))?0:1)"; then
  die "package.json 的 dependencies 仍有 link:/file: 本地路径，请改成正式版本号后再发布"
fi
printf '%s' "$BASE" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$' \
  || die "package.json 版本 $RAW 取不出 X.Y.Z 形式的正式版号（只接受 X.Y.Z 或 X.Y.Z-dev）"

DEV_VERSION="$BASE-dev.$(date -u +%Y%m%d%H%M%S)"
BACKUP="$(mktemp "${TMPDIR:-/tmp}/npm-package.json.XXXXXX")"
cp package.json "$BACKUP"
restore() { cp "$BACKUP" package.json; rm -f "$BACKUP"; }
trap restore EXIT

log "预览版本: $PKG_NAME@$DEV_VERSION（前缀 $BASE，通道 dev；package.json 当前 $RAW）"
node -e "const fs=require('fs');const p=JSON.parse(fs.readFileSync('package.json','utf8'));p.version='$DEV_VERSION';fs.writeFileSync('package.json',JSON.stringify(p,null,2)+'\n')"

log "构建: $BUILD_CMD"
$BUILD_CMD
log "测试: $TEST_CMD"
$TEST_CMD
log "发布: $PUBLISH_CMD"
$PUBLISH_CMD

restore
trap - EXIT
log "已发布 $PKG_NAME@$DEV_VERSION（dist-tag: dev）；package.json 已复原为 $RAW。"
