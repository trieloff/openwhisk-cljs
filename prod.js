var cljs = require("./src/js/openwhisk_cljs.core.js");
//console.log("See ma, a JS object", cljs)

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return cljs.greet(params);
}

console.log(main(null));

module.exports.default = main;
