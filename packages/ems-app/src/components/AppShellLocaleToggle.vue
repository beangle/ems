<template>
  <button
    type="button"
    class="bui-app-shell__locale-toggle"
    :aria-label="locale === 'zh-CN' ? 'Switch to English' : '切换到中文'"
    :title="locale === 'zh-CN' ? 'Switch to English' : '切换到中文'"
    @click="onToggle"
  >
    <span class="bui-app-shell__locale-toggle__glyph" aria-hidden="true">
      {{ locale === 'zh-CN' ? '中' : 'En' }}
    </span>
  </button>
</template>

<script setup lang="ts">
const props = defineProps<{
  locale?: 'zh-CN' | 'en-US'
}>()

const emit = defineEmits<{
  'update:locale': [locale: 'zh-CN' | 'en-US']
}>()

function onToggle() {
  const next = props.locale === 'zh-CN' ? 'en-US' : 'zh-CN'
  emit('update:locale', next)
}
</script>
