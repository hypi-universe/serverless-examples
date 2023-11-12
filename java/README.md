# Hypi's Fenrir Serverless Java runtime

The Java runtime has a few requirements:

* It MUST have a class called `app.hypi.fn.Main`
* It MUST have a method called `invoke` in `app.hypi.fn.Main` which accepts a `Map` as its only argument e.g. `public Map<String, Object> invoke(Map<String, Object> input)`
* `app.hypi.fn.Main` MUST have a no-arg constructor
* In your `Dockerfile`, place your function's JAR and all its dependencies in the `/home/hypi/fenrir/function` directory

Generally your function can have any dependencies it wants.

It is running in a Docker container and has access to the container filesystem for temporary files.
These are deleted when the function is cleaned up. 

A single container may be executed multiple times, this is not guaranteed and depends on many factors including the API request rate going to your function.
The higher the request rate to your function, the more likely it is that the same container will be sent multiple requests.

Each request will be sent to a new instance of `app.hypi.fn.Main` even if multiple requests are sent to the same container.

## Function input

The function's only argument must be a map. This map is mutable.

This map contains the following keys:

* `env` - a `Map<String,Object>` containing the values of any environment variables that the function was configured with 
* `args` - a `Map<String,Object>` containing the values of any parameters on the GraphQL field in the schema where the function is configured.
            For example, if the field is configured like `myApi(a: Int, b: String,c: MyObject) @fn(name:"my-fn")` then the args map will contain `a`, `b` and `c` with the values passed to `myApi` at runtime.
* hypi - a `Map<String,Object>` contains a set of keys that provide meta data for the function. Currently
  * `account_id: String` - the ID of the account that has invoked the function
  * `instance_id: String` - the ID of the Hypi app instance
  * `domain: String` - the domain of the instance (`instance_id` is for this domain), use it to make requests to Hypi from the function
  * `token: String` - the authorisation token used to make this request, normally corresponds to `account_id` but can be `anonymous`
  * `admin_token: String` - this is a token with escalated privileges on the domain. 
                            A function should NOT use this for most request, it allows the function to perform requests that the `token` user otherwise would not have permission to do.

                            It is intended to allow administrative operations or to call APIs restricted to normal users.

You're allowed to insert special keys into the map which can affect the behaviour of the function runtime.
Currently the following are supported:

* `output_format` - the only accepted value right now is `JSON` (which is also the default)

## Controlling output

The `invoke` method can have any return type that is serialisable by Jackson.
The example above returns a `Map<String,Object>` but it is only for illustration, you can create your own POJOs for example and return them.

# Example

First of all, make sure your Docker is authenticated with the Hypi Container registry
```shell
docker login hcr.hypi.app -u hypi
```

The username is always `hypi`, do not change it. When prompted for a password, copy the token from [here](https://console.hypi.app/developer-hub).


```java
package app.hypi.fn;

import java.util.Map;

public class Main {
  public Map<String, Object> invoke(Map<String, Object> input) {
    System.out.printf("ENV: %s", input.get("env"));
    System.out.printf("ARGS: %s", input.get("args"));
    System.out.flush();
    return input;
  }
}
```

`Dockerfile`

```dockerfile
FROM hypi/fenrir-runtime-java:v1

ADD target/fn-*.jar /home/hypi/fenrir/function/fn.jar
ADD target/lib/* /home/hypi/fenrir/function

```

`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>app.hypi.fenrir.google</groupId>
    <artifactId>fn-google-places</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>fn-google-places</name>
    <url>http://maven.apache.org</url>

    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.google.maps</groupId>
            <artifactId>google-maps-services</artifactId>
            <version>2.2.0</version>
        </dependency>
    </dependencies>
    <build>
        <finalName>${project.artifactId}-${project.version}</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.6.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <includeScope>compile</includeScope><!--Don't include test dependencies-->
                            <outputDirectory>${project.build.directory}/lib</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                    <release>17<!--${java.version}--></release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
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
