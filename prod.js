//require("./main.js");

const actionToRun = process.argv[2];

if (!actionToRun) {
  console.error("Missing argument <action-to-run>");
  console.error("Usage:");
  console.error("");
  console.error("  node test.js ./main.js <param1>=<value1>");
  process.exit(1);
}

let params = {};
for(var i=3;i<process.argv.length;i++) {
    let [name,value] = process.argv[i].split('=');
    params[name] = value;
}

const imports = require(actionToRun);
//support a non-exported main function as a fallback
const action = imports.main ? imports.main : main;

let result = action(params);

if (result.then) {
  Promise.resolve(result)
    .then(result => console.log(result))
    .catch(error => console.error(error));  
} else {
  console.log(result);
}