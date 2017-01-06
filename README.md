# Building

First, compile ClojureScript

````
lein cljsbuild once server-prod
````

Then, inline the Javascript

````
cat target/js/openwhisk_cljs.core.js prod.js > src/js/openwhisk_cljs.core.js
````

Test using

````
node src/js/openwhisk_cljs.core.js
````

# Deploy

Just commit to GitHub, your Webhook takes care of the rest