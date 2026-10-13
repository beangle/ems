#!/usr/bin/env bash
#
# 正式版本发布前校验（git-free）：
#   版本号校验 → 构建 → 测试 → 发布 dry-run。
#
# 用法：pnpm release:prepare        （或 bash scripts/release-prepare.sh）
#
# 本包位于混合仓库的 packages/ 下（仓库还承载 Scala 等其它产物、有自己的 tag 序列），
# 因此本脚本**不做任何 git 操作**（不 checkout / pull / commit / merge / 打 tag / push）：
# 分支与标签由仓库级流程负责，发布版本的唯一依据是 package.json 与 registry。
#
# 版本号约定（npm/semver，详见包内 README「发布」）：
#   - package.json 里始终是「下一个正式版号」的纯 X.Y.Z，不带 -SNAPSHOT 之类的后缀；
#   - 开发预览版由 ./scripts/release-dev.sh 生成 X.Y.Z-dev.<UTC 时间戳>，以 dist-tag `dev` 发布。
set -euo pipefail

PKG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PKG_DIR"
PKG_NAME="$(node -p "require('./package.json').name")"

BUILD_CMD="${BUILD_CMD:-pnpm build}"
TEST_CMD="${TEST_CMD:-pnpm test}"
DRY_RUN_CMD="${DRY_RUN_CMD:-pnpm publish --dry-run --no-git-checks}"

log() { printf '\033[1;36m[release]\033[0m %s\n' "$*"; }
die() { printf '\033[1;31m[release] 错误:\033[0m %s\n' "$*" >&2; exit 1; }

VERSION="$(node -p "require('./package.json').version")"
if node -e "const d=require('./package.json').dependencies||{};process.exit(Object.values(d).some(v=>/^(link|file):/.test(String(v)))?0:1)"; then
  die "package.json 的 dependencies 仍有 link:/file: 本地路径，请改成正式版本号后再发布"
fi
case "$VERSION" in
  *-*) die "package.json 版本 $VERSION 带预发布后缀；正式发布要求纯 X.Y.Z（开发预览请用 ./scripts/release-dev.sh）" ;;
  [0-9]*.[0-9]*.[0-9]*) ;;
  *) die "package.json 版本 $VERSION 不是 X.Y.Z 形式" ;;
esac
log "待发布: $PKG_NAME@$VERSION"

log "检查 $PKG_NAME@$VERSION 是否已在 registry 上存在"
if npm view "$PKG_NAME@$VERSION" version >/dev/null 2>&1; then
  die "$PKG_NAME@$VERSION 已存在于 registry：npm 不允许同号重发（消费方的 lockfile/store 会卡在旧字节），请先 bump 版本号"
fi

log "构建: $BUILD_CMD"
$BUILD_CMD
log "测试: $TEST_CMD"
$TEST_CMD
log "发布 dry-run: $DRY_RUN_CMD"
$DRY_RUN_CMD

log "校验通过。下一步（脚本不代做，publish 需要浏览器二次认证）："
log "  pnpm publish --tag latest --no-git-checks"
log "发布完成后，按仓库正常流程把 package.json 版本改为下一个 X.Y.Z 并提交；"
log "下一个版本号可运行 ./scripts/release-finish.sh 计算。"
