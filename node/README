rdio-simple for JavaScript (node.js)

An Rdio client including a built-in OAuth implementation.

It has been tested on the 0.4 branch of node.

To install the library simply add the om.js and rdio.js files to your source
directory.

Usage:
To use the library just load the Rdio class from the rdio module:
  var Rdio = require("rdio");
Create an Rdio instance passing in a tuple with your consumer key and secret:
  var rdio = new Rdio(["consumerkey", "consumersecret"])
Make API calls with the call(methodname, params) method:
  rdio.call('get', {'keys': 'a254895,a104386'})
Authenticate and authorize with the beginAuthentication and
completeAuthentication methods.

The current token (either request or access) is stored in rdio.token as an
array with the token and token secret.

Examples:
Both examples authenticate and then list the user's playlists. They use
credentials stored in rdio_consumer_credentials.js.
  examples/command-line.js
  examples/web-based.js
NOTE: web-based.js depends on Link (http://linkjs.org/). Install the dependency
in the examples directory by running:

  npm install link
