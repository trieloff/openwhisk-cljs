require("./target/js/goog/bootstrap/nodejs.js")
require("./target/js/figwheel4node_server_with_figwheel.js")

function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return openwhisk_cljs.core.greetjs(params);
}

var args = process.argv[2] ? JSON.parse(process.argv[2]) : null;

Promise.resolve(main(args)).then(function(value) {
  console.log("Resolving promise");
  console.log(value);
});

//console.log(process.argv[2]);

//console.log();
