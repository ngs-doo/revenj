const fs = require('fs');
const path = require('path');

const problematicIndexJsPath = path.resolve(
  __dirname,
  '../node_modules/symbol-observable/es/index.js',
);
const fixedPath = problematicIndexJsPath.replace(/\.js$/, '.mjs');

console.log('Revenj: running prebuild script');
if (fs.existsSync(problematicIndexJsPath)) {
  console.log(
    `Fixing dependency index file: ${problematicIndexJsPath} -> ${fixedPath}`,
  );
  fs.renameSync(problematicIndexJsPath, fixedPath);
}
