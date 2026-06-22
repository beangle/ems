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
