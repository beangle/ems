import type { Component } from 'vue'

/** Menu entry for AppShell sidebar. Titles should already be translated by the host app. */
export type AppShellMenuItem = {
  /** 路由路径；分组条目（带 children）不导航，可留空。 */
  path: string
  title: string
  icon?: Component
  /** 子菜单：非空时本条目渲染成可折叠的分组，子项缩进一级。 */
  children?: AppShellMenuItem[]
}

/** Business profile (场景) shown in AppShell topbar. */
export type AppShellProfile = {
  id: string | number
  name: string
}
