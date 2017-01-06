function main(params) {
  console.log("About to call ClojureScript. Wish me luck.");
  return module.exports.greet(params);
}

console.log(main(null));