#!/usr/bin/env python

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

# include the parent directory in the Python path
import sys,os.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from rdio import Rdio
from rdio_consumer_credentials import *
from urllib2 import HTTPError

# create an instance of the Rdio object with our consumer credentials
rdio = Rdio((RDIO_CONSUMER_KEY, RDIO_CONSUMER_SECRET))

try:
  # authenticate against the Rdio service
  url = rdio.begin_authentication('oob')
  print 'Go to: ' + url
  verifier = raw_input('Then enter the code: ').strip()
  rdio.complete_authentication(verifier)

  # find out what playlists you created
  myPlaylists = rdio.call('getPlaylists')['result']['owned']

  # list them
  for playlist in myPlaylists:
    print '%(shortUrl)s\t%(name)s' % playlist
except HTTPError, e:
  # if we have a protocol error, print it
  print e.read()
