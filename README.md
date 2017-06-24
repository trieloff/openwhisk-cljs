Running ClojureScript on OpenWhisk
==================================

Sample OpenWhisk action written in ClojureScript.

## Installing

First, compile ClojureScript

```bash
$ lein cljsbuild once server-prod
```

This command installs the node modules and generates the source code for the action at `main.js`.

During development, you might want to have the cljsbuild run continuously in the background:

```bash
$ lein cljsbuild auto server-prod
```

## Testing it locally

```bash
$ node test.js ./main.js param1=value1
```

Alternatively, you can also pass in parameters as a JSON object on stdin.

```bash
$ echo '{"foo":"bar"}' | node test.js ./main.js
```