<template>
  <main class="micro-shell">
    <router-view :key="route.fullPath" />
  </main>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<style>
/**
 * 子应用在 EMS 门户（Wujie）中的挂载壳层。
 *
 * 与 `registerWujieApp` 配对：后者负责挂载时机，本组件负责挂载后的根容器。
 * 页面自身的 panel 已有 16px 内边距，所以这里只留少量外边距；8px 是 4px 网格上的
 * 推荐值，需要更紧凑的应用可覆盖 `--microshell-padding`（例如设为 0）。
 */
.micro-shell {
  --microshell-padding: 8px;
  padding: var(--microshell-padding);
  min-height: 100%;
  box-sizing: border-box;
}
</style>
