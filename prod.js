var cljs = require("./target/js/figwheel4node-server.core.js");
//console.log("See ma, a JS object", cljs)

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return cljs.greet(params);
}

console.log(main(null));