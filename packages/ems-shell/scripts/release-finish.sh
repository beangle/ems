#!/usr/bin/env bash
#
# 计算并打印「下一个开发版本号」（只读：不写文件，不执行任何 git 操作）。
#
# 用法：pnpm release:finish [patch|minor|major]   （默认 patch，或 bash scripts/release-finish.sh）
#
# 本包在混合仓库的 packages/ 下，仓库级分支/提交/标签由仓库流程负责：
# 包级脚本既不碰 git，也不替你把改动写进工作区（否则会留下没人提交的脏文件）。
# 请按打印出的版本号手工修改 package.json，并走仓库正常提交流程。
set -euo pipefail

PKG_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PKG_DIR"
PKG_NAME="$(node -p "require('./package.json').name")"

BUMP="${1:-patch}"
case "$BUMP" in
  patch|minor|major) ;;
  *) echo "版本参数只能是 patch|minor|major" >&2; exit 1 ;;
esac

log() { printf '\033[1;36m[release]\033[0m %s\n' "$*"; }

CURRENT="$(node -p "require('./package.json').version")"
RELEASED="${CURRENT%%-*}"

NEXT="$(node -e "
const [maj, min, pat] = '$RELEASED'.split('.').map(Number)
let next
if ('$BUMP' === 'major') next = (maj + 1) + '.0.0'
else if ('$BUMP' === 'minor') next = maj + '.' + (min + 1) + '.0'
else next = maj + '.' + min + '.' + (pat + 1)
process.stdout.write(next)
")"

log "$PKG_NAME 当前 version: $CURRENT"
log "下一个开发版本: $NEXT"
log "请手工把 $PKG_DIR/package.json 的 version 改为 $NEXT，并按仓库正常流程提交（本脚本不做任何 git 操作）。"
