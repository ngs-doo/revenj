const { override, addBabelPlugin } = require('customize-cra');

// DSL-generated code leverages namespaces to create "rich enums", therefore we need to enable this flag
module.exports = override(
  addBabelPlugin([
      "@babel/plugin-transform-typescript",
      { allowNamespaces: true }
  ])
);
