rdio-simple for PHP
===================

An Rdio client including a built-in OAuth implementation.

Requirements
------------

* PHP5
* curl

To install the library simply add the `om.php` and `rdio.php` files to your source
directory.

Usage
-----

To use the library just require the `rdio.php` file in your PHP source file:
```php
require 'rdio.php';
```
Create an Rdio instance passing in an array with your consumer key and secret:
```php
$rdio = new Rdio(array("consumerkey", "consumersecret"));
```
Make API calls with the call(methodname, params) method:
```php
$rdio->call('get', keys='a254895,a104386')
```
Authenticate and authorize with the `begin_authentication` and
`complete_authentication` methods.

The current token (either request or access) is stored in `$rdio->token` as an
array with the token and token secret.

**Note:** PHP does not support unicode but the Rdio API always expects UTF-8
strings. If your strings are in latin-1 then you'll have to encode them to
UTF-8 using PHP's `utf8_encode` function before passing them into the library.

Examples
--------

Both examples authenticate and then list the user's playlists. They use
credentials stored in `rdio-consumer-credentials.php`.

* [examples/command-line.php](https://github.com/rdio/rdio-simple/blob/master/php/examples/command-line.php)
* [examples/web-based.php](https://github.com/rdio/rdio-simple/blob/master/php/examples/web-based.php)
