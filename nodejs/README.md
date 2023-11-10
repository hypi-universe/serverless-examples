# Hypi's Fenrir NodeJS serverless example

The NodeJS runtime has a few requirements:

* It MUST have a file called `/home/hypi/fenrir/function/package.json` which is a normal NodeJS package.json file defining your function's dependencies
* It MUST have a file at `/home/hypi/fenrir/function/main.js`
* The file MUST `exports.main` where `main` is a function that accepts two parameters (input and callback)

Generally your function can have any dependencies it wants.

It is running in a Docker container and has access to the container filesystem for temporary files.
These are deleted when the function is cleaned up.

A single container may be executed multiple times, this is not guaranteed and depends on many factors including the API request rate going to your function.
The higher the request rate to your function, the more likely it is that the same container will be sent multiple requests.

## Function input

The function has two parameters.
The first is a map, this map contains the following keys:

* `env` - an object containing the values of any environment variables that the function was configured with
* `args` - an object containing the values of any parameters on the GraphQL field in the schema where the function is configured.
  For example, if the field is configured like `myApi(a: Int, b: String,c: MyObject) @fn(name:"my-fn")` then the args map will contain `a`, `b` and `c` with the values passed to `myApi` at runtime.

The second parameter is a callback. This callback accept two parameters. 
Call the callback to send the response, the first parameter is for success and the second parameter is on error.
The error parameter MUST be an object with the fields `code` and `message`. 
The `code` field of the object is a number and the `message` field is a string describing the error

## Controlling output

The function can set `input.format = 'JSON'` to set the output format. 
`JSON` is currently the only supported format and so is the default.

# Example

First of all, make sure your Docker is authenticated with the Hypi Container registry
```shell
docker login hcr.hypi.app -u hypi
```

The username is always `hypi`, do not change it. When prompted for a password, copy the token from [here](https://console.hypi.app/developer-hub).

If you create `src/main.js` with the content:
```javascript
/**
 * @param input has env and args
 * @param callback accepts two params, 1st one is the response to return and second is error if there is one
 */
function hellWorld(input, callback) {
    console.log('Yeah, we got some input', input, input.env, input.args);
    callback(input, null);
}

// Must export `main`
exports.main = hellWorld;
```

`package.json`
```json
{
  "name": "fenrir-runtime-nodejs-example",
  "version": "0.1.0",
  "scripts": {
    "build": "docker build . -t hcr.hypi.app/nodejs-example/$VERSION",
    "deploy": "docker push hcr.hypi.app/nodejs-example/$VERSION"
  },
  "dependencies": {
  }
}
```

`Dockerfile`
```dockerfile
FROM hypi/fenrir-runtime-nodejs:v1

ADD src/main.js /home/hypi/fenrir/function/
RUN /home/hypi/fenrir/build.sh

WORKDIR /home/hypi/fenrir/function
```

## Build & Deploy

You can then:
1. `VERSION=v1 npm run build`
2. `VERSION=v1 npm run deploy`
3. In your Hypi app at [console.hypi.app](https://console.hypi.app) reference your function in the schema like 
```graphql
type Query {
  myFnName(a: Int, b: String, c: Float, d: Boolean, e: Json, f: MyType): Json @fn(name:"nodejs-example", version: "v1", env: ["abc"]) 
}
```
4. Call the function with the [GraphQL or REST API](https://docs.hypi.app/docs/lowcode/apisetup)