import type { NavProfile } from './types.js';

export interface EmsConfig {
  profiles: NavProfile[];
  profile: NavProfile;
  api: string;
}

export const config: EmsConfig = { profiles: [], profile: {} as NavProfile, api: '' };

function processProfileUrl(): void {
  if (typeof location === 'undefined') return;
  for (let i = 0; i < config.profiles.length; i++) {
    const profile = config.profiles[i];
    if (!profile.url) {
      profile.url = location.origin + location.pathname + '?contextProfileId=' + profile.id;
    }
  }
}

function resolveProfileId(
  profileId?: string | number | { profile?: string | number } | null
): string | null {
  if (profileId != null && typeof profileId === 'object') {
    const fromCookie = (profileId as { profile?: string | number }).profile;
    if (fromCookie != null && String(fromCookie).trim() !== '' && String(fromCookie) !== 'null') {
      return String(fromCookie);
    }
  } else if (profileId != null && String(profileId).trim() !== '' && String(profileId) !== 'null') {
    return String(profileId);
  }
  if (typeof location !== 'undefined' && location.search) {
    try {
      const q = new URLSearchParams(location.search).get('contextProfileId');
      if (q != null && q.trim() !== '') return q.trim();
    } catch {
      /* ignore */
    }
  }
  return null;
}

/**
 * @param profiles 用户可用 profile 列表
 * @param profileId 当前选中的 profile id（字符串）；也兼容旧 cookie JSON `{profile:...}`；空则尝试 URL `contextProfileId`，再取列表第一项
 */
export function init(
  profiles: NavProfile[],
  profileId?: string | number | { profile?: string | number } | null
): void {
  let profile: NavProfile | null = null;
  const pid = resolveProfileId(profileId);
  if (pid != null && profiles && profiles.length > 0) {
    for (let i = 0; i < profiles.length; i++) {
      const p = profiles[i];
      if (p != null && String(p.id) === pid) {
        profile = p;
        break;
      }
    }
  }
  if (!profile && profiles && profiles.length > 0) {
    profile = profiles[0];
  }
  config.profiles = profiles || [];
  config.profile = profile || ({} as NavProfile);
  processProfileUrl();
}
