var openwhisk_cljs = require("./target/js/openwhisk_cljs.core.js");

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return openwhisk_cljs.greet(params);
}

console.log(main(null));

module.exports.default = main;
