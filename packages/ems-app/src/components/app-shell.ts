import type { Component } from 'vue'

/** Menu entry for AppShell sidebar. Titles should already be translated by the host app. */
export type AppShellMenuItem = {
  path: string
  title: string
  icon?: Component
}

/** Business profile (场景) shown in AppShell topbar. */
export type AppShellProfile = {
  id: string | number
  name: string
}
