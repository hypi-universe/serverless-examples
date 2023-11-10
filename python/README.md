# Hypi's Fenrir Serverless Python runtime

The Python runtime has a few requirements:

* It MUST have a file called `/home/hypi/fenrir/function/main.py` it is the entry point for the function and requires a `async def invoke(request: dict)` definition

Generally your function can have any dependencies it wants in `requirements.txt` or otherwise globally installed in the image.

It is running in a Docker container and has access to the container filesystem for temporary files.
These are deleted when the function is cleaned up.

A single container may be executed multiple times, this is not guaranteed and depends on many factors including the API request rate going to your function.
The higher the request rate to your function, the more likely it is that the same container will be sent multiple requests.

## Function input

The function has one dictionary parameter. It has a few entries 

* `env` - an object containing the values of any environment variables that the function was configured with
* `args` - an object containing the values of any parameters on the GraphQL field in the schema where the function is configured.
  For example, if the field is configured like `myApi(a: Int, b: String,c: MyObject) @fn(name:"my-fn")` then the args map will contain `a`, `b` and `c` with the values passed to `myApi` at runtime.

## Controlling output

The function can set `request.format = 'JSON'` to set the output format.
`JSON` is currently the only supported format and so is the default.

# Example

First of all, make sure your Docker is authenticated with the Hypi Container registry
```shell
docker login hcr.hypi.app -u hypi
```

The username is always `hypi`, do not change it. When prompted for a password, copy the token from [here](https://console.hypi.app/developer-hub).

Create `src/main.py` with the content:
```python
# request.env and request.args are available
# return the object you want the function to send as a response
# throw exceptions to indicate an error
async def invoke(request: dict):
    print(request)
    return request
```

`requirements.txt`
```python
#add any python dependencies your function needs
```

`Dockerfile`
```dockerfile
FROM hypi/fenrir-runtime-python:v1

COPY src/main.py /home/hypi/fenrir/function/main.py
#If you have dependencies specified in requirements.txt then add a line like this
#RUN cd /home/hypi/fenrir/function; pip install -r requirements.txt
#OR just run pip install
#RUN pip install my-dependency
```

## Build & Deploy

1. Build your function `docker build . -t hcr.hypi.app/my-fn:v1`
2. Deploy your function `docker push hcr.hypi.app/my-fn:v1`
3. In your Hypi app at [console.hypi.app](https://console.hypi.app) reference your function in the schema like 
```graphql
type Query {
  myFnName(a: Int, b: String, c: Float, d: Boolean, e: Json, f: MyType): Json @fn(name:"google-places", version: "v1.1", env: ["abc"]) 
}
```
4. Call the function with the [GraphQL or REST API](https://docs.hypi.app/docs/lowcode/apisetup)