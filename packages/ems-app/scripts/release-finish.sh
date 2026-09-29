#!/usr/bin/env bash
#
# 发布收尾：读取当前（main 上）package.json 的已发布版本，
# 回到 develop 把版本号设为下一个 -SNAPSHOT 并提交推送。
# 用法：./scripts/release-finish.sh [patch|minor|major]   （默认 patch）
#
# 注意：本包是 beangle/ems 仓库的 workspace 成员，脚本里的 git 操作作用于
# 整个 ems 仓库（只改 packages/ems-app/package.json）。
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

BUMP="${1:-patch}"
case "$BUMP" in
  patch|minor|major) ;;
  *) echo "版本参数只能是 patch|minor|major" >&2; exit 1 ;;
esac

log() { printf '\033[1;36m[release]\033[0m %s\n' "$*"; }
die() { printf '\033[1;31m[release] 错误:\033[0m %s\n' "$*" >&2; exit 1; }

if [ -n "$(git status --porcelain)" ]; then
  die "工作区有未提交改动，请先提交或暂存"
fi

RELEASED="$(node -p "require('./package.json').version")"
case "$RELEASED" in
  *-SNAPSHOT) die "当前 package.json 版本 $RELEASED 仍是 -SNAPSHOT，请确认已发布后再执行收尾" ;;
esac
log "已发布版本: $RELEASED（来自 package.json）"

log "切换到 develop"
git checkout develop
git pull --ff-only origin develop 2>/dev/null || log "develop 无远端跟踪或已最新（忽略 pull）"

NEXT="$(node -e "
const [maj, min, pat] = '$RELEASED'.split('.').map(Number)
let next
if ('$BUMP' === 'major') next = (maj + 1) + '.0.0'
else if ('$BUMP' === 'minor') next = maj + '.' + (min + 1) + '.0'
else next = maj + '.' + min + '.' + (pat + 1)
process.stdout.write(next)
")-SNAPSHOT"

log "设置下一个开发版本: $NEXT"
node -e "const fs=require('fs');const p=JSON.parse(fs.readFileSync('package.json','utf8'));p.version='$NEXT';fs.writeFileSync('package.json',JSON.stringify(p,null,2)+'\n')"
git add package.json
git commit -m "chore: next development version $NEXT"
git push origin develop
