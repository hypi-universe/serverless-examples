# Fenrir: Hypi's Serverless Engine

Fenrir leverages existing, widely available technology to provide its functionality.

Generally, you can implement and test your functions in your preferred language without any Hypi dependencies.
This works because the arguments to a function are provided through the language's native `Map`/`Struct`/`Object` implementation or equivalent.

All implementations have a predictable entry point (e.g. for Java a specific class, for NodeJS a specific file) and the entrypoint then has an `invoke` method which accepts a single object as its argument.

The value returned from the `invoke` method is treated as the response sent from the serverless function and this can be any type serialisable to JSON.

For specific instructions, check the README of each language.

* [Java](java/README.md)
* [NodeJS](nodejs/README.md)
* [Python](python/README.md)
