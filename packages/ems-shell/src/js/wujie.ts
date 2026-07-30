export function resolveWujieRuntime(): NonNullable<Window['wujie']> | null {
  if (typeof window !== 'undefined' && window.wujie?.startApp) {
    return window.wujie;
  }
  try {
    if (typeof beangle !== 'undefined' && beangle.amd?.pickModuleExport) {
      const picked = beangle.amd.pickModuleExport('wujie') as NonNullable<Window['wujie']> | undefined;
      if (picked?.startApp) {
        window.wujie = picked;
        return picked;
      }
    }
  } catch {
    /* ignore */
  }
  return null;
}

/** 向无界总线发事件（alive 子应用可 $on）；运行时未就绪时静默跳过 */
export function emitWujieBus(event: string, ...args: unknown[]): void {
  if (!event) return;
  try {
    const bus = resolveWujieRuntime()?.bus ?? (typeof window !== 'undefined' ? window.wujie?.bus : undefined);
    bus?.$emit?.(event, ...args);
  } catch {
    /* ignore */
  }
}
