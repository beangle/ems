import fs from 'fs';
import path from 'path';
import { execSync } from 'child_process';
import { fileURLToPath } from 'url';
import { buildAll } from './build.mjs';

const root = path.dirname(fileURLToPath(import.meta.url));
const pkg = JSON.parse(fs.readFileSync(path.join(root, 'package.json'), 'utf8'));
const version = pkg.version;
const releaseDir = path.join(root, 'release');
const bundleName = `ems-shell-${version}`;
const stagingDir = path.join(releaseDir, 'staging');
const deployRoot = path.join(stagingDir, 'ems-shell', version);
const zipPath = path.join(releaseDir, `${bundleName}.zip`);

function copyDir(src, dest) {
  fs.mkdirSync(dest, { recursive: true });
  for (const name of fs.readdirSync(src)) {
    const from = path.join(src, name);
    const to = path.join(dest, name);
    if (fs.statSync(from).isDirectory()) {
      copyDir(from, to);
    } else {
      fs.copyFileSync(from, to);
    }
  }
}

await buildAll();

const dist = path.join(root, 'dist');
if (!fs.existsSync(dist)) {
  console.error('dist/ not found after build');
  process.exit(1);
}

fs.rmSync(stagingDir, { recursive: true, force: true });
fs.mkdirSync(releaseDir, { recursive: true });
copyDir(dist, deployRoot);

fs.rmSync(zipPath, { force: true });
execSync(`zip -rq ${JSON.stringify(`${bundleName}.zip`)} ems-shell`, {
  cwd: stagingDir,
  stdio: 'inherit',
});

fs.rmSync(stagingDir, { recursive: true, force: true });
console.log(`package → ${zipPath} (ems-shell/${version}/…)`);
