module.exports = {
  root: true,
  env: {
    node: true,
    "es6": true
  },
  extends: [
    "eslint:recommended",
    "google",
  ],
  rules: {
    quotes: ["error", "double"],
  },
};
