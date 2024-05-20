module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  extends: [
    "eslint:recommended",
    "plugin:import/errors",
    "plugin:import/warnings",
    "plugin:import/typescript",
    "google",
    "plugin:@typescript-eslint/recommended",
  ],
  parser: "@typescript-eslint/parser",
  parserOptions: {
    project: ["tsconfig.json", "tsconfig.dev.json"],
    sourceType: "module",
  },
  ignorePatterns: [
    "/lib/**/*", // Ignore built files.
    "/generated/**/*", // Ignore generated files.
  ],
  plugins: [
    "@typescript-eslint",
    "import",
  ],
  rules: {
    "valid-jsdoc" : 0,
    "indent": "off",
    "spaced-comment": 0,
    "key-spacing": 0,
    "space-before-function-paren": 0,
    "require-jsdoc": 0,
    "object-curly-spacing": 0,
    "quotes": 0,
    "import/no-unresolved": 0,
    "max-line": "off",
  },
};
