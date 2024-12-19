const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const problematicIndexJsPath = path.resolve(
  __dirname,
  '../node_modules/symbol-observable/es/index.js',
);
const fixedPath = problematicIndexJsPath.replace(/\.js$/, '.mjs');

try {
  console.log('Running tests before proceeding with prebuild script...');
  execSync('yarn test', { stdio: 'inherit' });
  console.log('Tests passed successfully.');
} catch (error) {
  console.error('Tests failed. Exiting prebuild script.');
  process.exit(1);
}

console.log('Revenj: running prebuild script');
if (fs.existsSync(problematicIndexJsPath)) {
  console.log(
    `Fixing dependency index file: ${problematicIndexJsPath} -> ${fixedPath}`,
  );
  fs.renameSync(problematicIndexJsPath, fixedPath);
}
