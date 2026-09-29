<template>
  <button
    type="button"
    class="bui-app-shell__fullscreen-toggle"
    :aria-label="isFullscreen ? '退出全屏' : '进入全屏'"
    :title="isFullscreen ? '退出全屏' : '进入全屏'"
    @click="onToggle"
  >
    <FullscreenExitIcon v-if="isFullscreen" size="18px" />
    <FullscreenIcon v-else size="18px" />
  </button>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { FullscreenExitIcon, FullscreenIcon } from 'tdesign-icons-vue-next'

const isFullscreen = ref(false)

function updateState() {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

function onToggle() {
  if (!document.fullscreenEnabled) return
  if (document.fullscreenElement) {
    void document.exitFullscreen()
  } else {
    void document.documentElement.requestFullscreen()
  }
}

onMounted(() => {
  document.addEventListener('fullscreenchange', updateState)
  updateState()
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', updateState)
})
</script>
