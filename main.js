require("./target/js/goog/bootstrap/nodejs.js")
require("./target/js/figwheel4node_server_with_figwheel.js")

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return openwhisk_cljs.core.greetjs(params);
}



console.log(main({"whorocks":"you"}));