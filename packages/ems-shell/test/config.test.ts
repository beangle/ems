import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { config, init } from '../src/js/config.ts';

describe('emsShell.init profile selection', () => {
  const profiles = [
    { id: '1', name: 'A' },
    { id: '5', name: 'B' },
    { id: 7, name: 'C' },
  ];

  it('selects by string profileId', () => {
    init(profiles as any, '5');
    assert.equal(config.profile.name, 'B');
  });

  it('selects by legacy cookie object', () => {
    init(profiles as any, { profile: '5' });
    assert.equal(config.profile.name, 'B');
  });

  it('selects numeric id in list via string arg', () => {
    init(profiles as any, '7');
    assert.equal(config.profile.name, 'C');
  });

  it('falls back to first when missing', () => {
    init(profiles as any, null);
    assert.equal(config.profile.name, 'A');
  });
});
