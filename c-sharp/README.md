RdioSimple for C# / .NET
========================

An Rdio web service API client including a built-in OAuth implementation.

This code doesn't have any dependencies outside of the core C#/.NET 2.0 
framework. The example uses LINQ and JSON.NET.

---

To use Rdio.Simple in your project just include the `OAuth.cs` and 
`RdioClient.cs` files in your project and use the `Rdio.Simple` namespace:
```c#
using Rdio.Simple;
```

The main class that you'll interact with is `Rdio.Simple.RdioClient`. It's an 
immutable class that holds the Rdio consumer key and secret, and optionally
an OAuth access token:
```c#
RdioClient anon = new RdioClient(new RdioClient.Consumer("consumerkey", "consumersecret"));
RdioClient authed = new RdioClient(new RdioClient.Consumer("consumerkey", "consumersecret"),
                                   new RdioClient.Token("accesstoken", "accesstokensecret"));
```

The Call method on RdioClient makes API calls. A string containing the full
JSON response. Applications should use a JSON parsing library (such as
JSON.NET) to parse the response. Method arguments can be passed as an
`IDictionary<string,string>`:
```c#
string me = rdioClient.Call("currentUser");
string eleven = rdioClient.Call("search", new Dictionary<string,string> {
  { "query", "spinal tap" },
  { "types", "Album", }
});
```

Authentication is a two step process. First call the `BeginAuthentication` with
a callback URL (or "oob" to use the PIN flow):
```c#
RdioClient.AuthState state = rdioClient.BeginAuthentication("http://example.com/callback");
```

The `RdioClient.AuthState` object has an `Url` that the user should be directed
to to approve the application and a `RequestToken` that should be saved for the
next step in the process.

When the callback URL is loaded (or the user has entered the PIN) call
CompleteAuthentication with the `oauth_verifier` query-string parameter (or
the PIN) and the request token returned from `BeginAuthentication`:
```c#
OAuth.Token accessToken = rdioClient.CompleteAuthentication(verifier,
  state.RequestToken);
```
And then make a new RdioClient object with that access token:
```c#
rdioClient = new RdioClient(rdioClient.Consumer, accessToken)
```

There is an example in the [Examples](https://github.com/rdio/rdio-simple/tree/master/c-sharp/Examples)
directory that authenticates a user and then lists their playlists.

To use it make a copy of the `RdioConsumerCredentialsEXAMPLE.cs` as
`RdioConsumerCredentials.cs` and fill in valid application
credentials from http://developer.rdio.com/.
