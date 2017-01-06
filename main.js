require("./target/js/figwheel4node_server_with_figwheel.js");
var cljs = require("./target/js/openwhisk_cljs/core.js");
//console.log("See ma, a JS object", cljs)

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return cljs.greet(params);
}

console.log(main(null));