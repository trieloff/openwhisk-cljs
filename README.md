Running ClojureScript on OpenWhisk
==================================

Sample OpenWhisk action written in ClojureScript.

## Installing

First, compile ClojureScript

```bash
$ lumo -c src/cljs build.cljs
```
This should create/update the file `main.js`. Alternatively, you can use leiningen.

```bash
$ lein cljsbuild once server-prod
```

This command installs the node modules and generates the source code for the action at `main.js`.

## Testing it locally

```bash
$ node test.js ./main.js param1=value1
```

Alternatively, you can also pass in parameters as a JSON object on stdin.

```bash
$ echo '{"foo":"bar"}' | node test.js ./main.js
```