#!/usr/bin/env bash
#
# 发布准备（SNAPSHOT 模式）：
#   develop 上构建/测试/发布 dry-run 通过 → 去掉 -SNAPSHOT → 提交并打 tag
#   → 切 main 合并 develop → push main + tag
# 用法：./scripts/release-prepare.sh [patch|minor|major]   （默认 patch）
# 可用环境变量覆盖：BUILD_CMD / TEST_CMD / DRY_RUN_CMD
#
# 注意：本包是 beangle/ems 仓库的 workspace 成员，脚本里的 git 操作作用于
# 整个 ems 仓库（只改 packages/ems-app/package.json），tag 加包名前缀
# 以免与 ems 自身的 tag（v4.x）混淆。
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

BUMP="${1:-patch}"
case "$BUMP" in
  patch|minor|major) ;;
  *) echo "版本参数只能是 patch|minor|major" >&2; exit 1 ;;
esac

BUILD_CMD="${BUILD_CMD:-pnpm build}"
TEST_CMD="${TEST_CMD:-pnpm test}"
DRY_RUN_CMD="${DRY_RUN_CMD:-pnpm publish --dry-run --no-git-checks}"
TAG_PREFIX="${TAG_PREFIX:-ems-app-v}"

log() { printf '\033[1;36m[release]\033[0m %s\n' "$*"; }
die() { printf '\033[1;31m[release] 错误:\033[0m %s\n' "$*" >&2; exit 1; }

if [ -n "$(git status --porcelain)" ]; then
  die "工作区有未提交改动，请先提交或暂存"
fi

START_BRANCH="$(git branch --show-current)"
if [ "$START_BRANCH" != "develop" ]; then
  log "切换到 develop"
  git checkout develop
fi
log "更新 develop 并确保最新"
if ! git pull --ff-only origin develop; then
  die "develop 无法快进更新（本地与远端分叉或拉取失败），发布停止"
fi

log "构建门禁: $BUILD_CMD"
$BUILD_CMD
log "测试门禁: $TEST_CMD"
$TEST_CMD
log "发布 dry-run: $DRY_RUN_CMD"
$DRY_RUN_CMD

VERSION="$(node -p "require('./package.json').version")"
case "$VERSION" in
  *-SNAPSHOT) ;;
  *) die "当前版本 $VERSION 不是 -SNAPSHOT，请先在 develop 上把版本号改为 x-SNAPSHOT" ;;
esac
REL="${VERSION%-SNAPSHOT}"

log "去掉 SNAPSHOT: $VERSION -> $REL"
node -e "const fs=require('fs');const p=JSON.parse(fs.readFileSync('package.json','utf8'));p.version='$REL';fs.writeFileSync('package.json',JSON.stringify(p,null,2)+'\n')"
git add package.json
git commit -m "release: v$REL"
git tag "${TAG_PREFIX}$REL"

log "切换到 main 并合并 develop"
git checkout main
git pull --ff-only origin main 2>/dev/null || log "main 无远端跟踪或已最新（忽略 pull）"
if ! git merge develop -m "release: v$REL"; then
  git checkout develop 2>/dev/null || true
  die "合并冲突，请手动处理（git merge --abort 后处理冲突）"
fi

log "推送 main 与 tag ${TAG_PREFIX}$REL"
git push origin main
git push origin "${TAG_PREFIX}$REL"

log "发布准备完成，当前在 main 分支。后续："
log "  1. pnpm publish（发布正式版本 v$REL）"
log "  2. 发布完成后执行 ./scripts/release-finish.sh $BUMP（自动读取版本并设置下一个 -SNAPSHOT）"
