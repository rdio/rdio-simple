#rdio-simple for Java

**An Rdio client including a built-in OAuth implementation.**

This has library only depends on libraries included by default in recent
versions of Java. It has been tested with Java 1.6.0.

To install the library simply copy the contents of the src/ directory into
your source code. You'll need to include: `com.rdio.simple.RdioClient`,
`com.rdio.simple.Om`, `com.rdio.simple.Parameters` and either 
`com.rdio.simple.RdioCoreClient` or `com.rdio.simple.RdioApacheClient`.

`RdioCoreClient` makes API calls using Java's `java.net.HttpURLConnection`
while `RdioApacheClient` builds on the 
[Apache HttpClient](http://hc.apache.org/).

##Usage
To use the library just load the `com.rdio.simple` package:

    import com.rdio.simple.*;
  
Create an `RdioClient` instance passing in an `RdioClient.Consumer` object:

    RdioClient rdio = new RdioCoreClient(new RdioClient.Consumer("consumerkey", "consumersecret"));

and optionally an `RdioClient.Token` access token object:

    RdioClient rdio = new RdioCoreClient(new RdioClient.Consumer("consumerkey", "consumersecret")
                                         new RdioClient.Token("tokenkey", "tokensecret"));
  
Make API calls with the `call(methodname, params)` method:

    rdio.call("get", Parameters.build("keys", "a254895,a104386"));

Authenticate and authorize with the `beginAuthentication` and
`completeAuthentication` methods.

The library is somewhat thread-safe. All of the objects are immutable and there is no stored state.

##Examples
There is an example that uses the rdio-simple Java library to list a user's
playlists. Compile it with:

    ant compile

and run it with:

    java -cp classes/ com.rdio.simple.examples.CommandLine.

Before you can compile or run it you'll have to create a:

    examples/com/rdio/simple/examples/ConsumerCredentials.java

with consumer credentials from http://developer.rdio.com/

There's an additional example available at:

    https://github.com/rdio/rdio-simple-play-example

It's a web based example that uses the Play! framework.
