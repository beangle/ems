import type { NavProfile } from './types.js';

export interface EmsConfig {
  profiles: NavProfile[];
  profile: NavProfile;
  api: string;
}

export const config: EmsConfig = { profiles: [], profile: {} as NavProfile, api: '' };

function processProfileUrl(): void {
  for (let i = 0; i < config.profiles.length; i++) {
    const profile = config.profiles[i];
    if (!profile.url) {
      profile.url = location.origin + location.pathname + '?contextProfileId=' + profile.id;
    }
  }
}

export function init(
  profiles: NavProfile[],
  cookie?: { profile?: string | number } | null
): void {
  let profile: NavProfile | null = null;
  if (cookie?.profile != null) {
    for (let i = 0; i < profiles.length; i++) {
      const p = profiles[i];
      if (p.id == cookie.profile) {
        profile = p;
        break;
      }
    }
  }
  if (!profile) {
    profile = profiles[0];
  }
  config.profiles = profiles;
  config.profile = profile;
  processProfileUrl();
}
