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
$ node test.js ./main.js url=http://stackoverflow.com/questions/43791970/pandas-assigning-columns-with-multiple-conditions-and-date-thresholds
```

Specify your StackExchange API key in the `key` parameter if you run into rate limit issues.