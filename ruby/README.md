rdio-simple for Ruby
====================

An Rdio client including a built-in OAuth implementation.

It's built for Ruby 1.8. Its only dependency is the JSON gem. It probably
works in Ruby 1.9 and other Ruby environments, but that hasn't been tested.

To install the library simply add the `om.rb` and `rdio.rb` files to your source
directory.

Usage
-----

To use the library just load the Rdio class from the rdio module:
```ruby
require 'rdio'
```
Create an Rdio instance passing in an array with your consumer key and secret:
```ruby
rdio = Rdio.new(["consumerkey", "consumersecret"])
```
Make API calls with the `call(methodname, params)` method:
```ruby
rdio.call('get', {'keys'=>'a254895,a104386'})
```
Authenticate and authorize with the `begin_authentication` and
`complete_authentication` methods.

The current token (either request or access) is stored in `rdio.token` as an
array with the token and token secret.

Examples
--------
Both examples authenticate and then list the user's playlists. They use
credentials stored in `rdio_consumer_credentials.rb`.

* [examples/command-line.rb](https://github.com/rdio/rdio-simple/blob/master/ruby/examples/command-line.rb)
* [examples/web-based.rb](https://github.com/rdio/rdio-simple/blob/master/ruby/examples/web-based.rb)

**NOTE:** `web-based.rb` depends on Sinatra.
