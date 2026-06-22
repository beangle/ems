import * as esbuild from 'esbuild';
import fs from 'fs';
import path from 'path';
import { fileURLToPath, pathToFileURL } from 'url';

const root = path.dirname(fileURLToPath(import.meta.url));
const dist = path.join(root, 'dist');
const watch = process.argv.includes('--watch');

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

async function buildJs(minify) {
  const suffix = minify ? '-min' : '';
  ensureDir(path.join(dist, 'js'));
  await esbuild.build({
    entryPoints: [path.join(root, 'src/js/index.ts')],
    outfile: path.join(dist, `js/ems-shell${suffix}.js`),
    bundle: true,
    minify,
    sourcemap: !minify,
    format: 'iife',
    target: ['es2018'],
    legalComments: 'none',
  });
}

function copyDir(src, dest) {
  if (!fs.existsSync(src)) return;
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

async function buildCss() {
  ensureDir(path.join(dist, 'css'));
  const cssEntry = path.join(root, 'src/css/ems-shell.css');
  await esbuild.build({
    entryPoints: [cssEntry],
    outfile: path.join(dist, 'css/ems-shell-min.css'),
    minify: true,
  });
  fs.copyFileSync(cssEntry, path.join(dist, 'css/ems-shell.css'));
  fs.copyFileSync(
    path.join(root, 'src/css/login.css'),
    path.join(dist, 'css/login.css')
  );
}

function copyImages() {
  copyDir(path.join(root, 'src/images'), path.join(dist, 'images'));
}

export async function buildAll() {
  await buildJs(false);
  await buildJs(true);
  await buildCss();
  copyImages();
  console.log('built → dist/');
}

function isMain() {
  const entry = process.argv[1];
  return entry && import.meta.url === pathToFileURL(path.resolve(entry)).href;
}

if (watch) {
  const ctx = await esbuild.context({
    entryPoints: [path.join(root, 'src/js/index.ts')],
    outfile: path.join(dist, 'js/ems-shell.js'),
    bundle: true,
    sourcemap: true,
    format: 'iife',
    target: ['es2018'],
    legalComments: 'none',
  });
  await ctx.watch();
  console.log('watching ems-shell sources…');
} else if (isMain()) {
  await buildAll();
}
