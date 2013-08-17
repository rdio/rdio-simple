rdio-simple for Python
======================

An Rdio client including a built-in OAuth implementation.

This library only depends on libraries included by default in recent
versions of Python. It has been tested with Python 2.6.

To install the library simply add the `om.py` and `rdio.py` files to your source
directory.

Usage
-----

To use the library just load the Rdio class from the `rdio` module:
```python
from rdio import Rdio
```
Create an Rdio instance passing in a tuple with your consumer key and secret:
```python
rdio = Rdio(("consumerkey", "consumersecret"))
```
Make API calls with the call(methodname, params) method:
```python
rdio.call('get', {'keys':'a254895,a104386'})
```
Authenticate and authorize with the `begin_authentication` and
`complete_authentication` methods.

The current token (either request or access) is stored in `rdio.token` as a
tuple with the token and token secret.

Examples
--------

Both examples authenticate and then list the user's playlists. They use
credentials stored in `rdio-consumer-credentials.py`.

* [examples/command-line.py](https://github.com/rdio/rdio-simple/blob/master/python/examples/command-line.py)
* [examples/web-based.py](https://github.com/rdio/rdio-simple/blob/master/python/examples/web-based.py)

**NOTE:** `web-based.py` depends on [web.py](http://www.webpy.org/)

