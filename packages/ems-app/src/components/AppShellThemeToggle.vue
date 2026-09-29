<template>
  <button
    type="button"
    class="bui-app-shell__theme-toggle"
    :aria-label="mode === 'dark' ? '切换到浅色模式' : '切换到深色模式'"
    :title="mode === 'dark' ? '切换到浅色模式' : '切换到深色模式'"
    @click="onToggle"
  >
    <MoonIcon v-if="mode === 'light'" size="18px" />
    <SunnyIcon v-else size="18px" />
  </button>
</template>

<script setup lang="ts">
import { MoonIcon, SunnyIcon } from 'tdesign-icons-vue-next'

const props = defineProps<{
  mode?: 'light' | 'dark'
}>()

const emit = defineEmits<{
  'update:theme-mode': [mode: 'light' | 'dark']
}>()

function onToggle() {
  const next = props.mode === 'dark' ? 'light' : 'dark'
  emit('update:theme-mode', next)
}
</script>
