# (c) 2011 Rdio Inc
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

from __future__ import unicode_literals

try:
    from urllib.request import urlopen, Request
    from urllib.parse import urlencode
    from urllib.parse import parse_qsl
except ImportError:
    from urllib2 import urlopen, Request
    from urllib import urlencode
    from urlparse import parse_qsl

import json
import sys

from om import om


PY3 = (sys.version_info >= (3, 0, 0))


class Rdio:
  def __init__(self, consumer, token=None):
    self.__consumer = consumer
    self.token = token
    self.content_type = 'application/x-www-form-urlencoded;charset=utf-8'

  def __signed_post(self, url, params):
    auth = om(self.__consumer, url, params, self.token)
    if not PY3:
        encoded_params = {}
        for k, v in params.items():
            encoded_params[k.encode('utf-8')] = v.encode('utf-8')
        params = encoded_params
    # Second parameter to Request should be a bytes (Python3) or str (Python2)
    # Since we are using unicode everywhere, we should do an encode
    # and set Content-Type header accordingly
    req = Request(url, urlencode(params).encode('utf-8'),
                  {'Authorization': auth, 'Content-Type': self.content_type})
    res = urlopen(req)
    # return unicode instead of bytes
    return res.read().decode('utf-8')

  def begin_authentication(self, callback_url):
    # request a request token from the server
    response = self.__signed_post('http://api.rdio.com/oauth/request_token',
      {'oauth_callback': callback_url})
    # parse the response
    parsed = dict(parse_qsl(response))
    # save the token
    self.token = (parsed['oauth_token'], parsed['oauth_token_secret'])
    # return an URL that the user can use to authorize this application
    return parsed['login_url'] + '?oauth_token=' + parsed['oauth_token']

  def complete_authentication(self, verifier):
    # request an access token
    response = self.__signed_post('http://api.rdio.com/oauth/access_token',
        {'oauth_verifier': verifier})
    # parse the response
    parsed = dict(parse_qsl(response))
    # save the token
    self.token = (parsed['oauth_token'], parsed['oauth_token_secret'])

  def call(self, method, params=dict()):
    # make a copy of the dict
    params = dict(params)
    # put the method in the dict
    params['method'] = method
    # call to the server and parse the response
    return json.loads(self.__signed_post('http://api.rdio.com/1/', params))

