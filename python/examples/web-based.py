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

# import the rdio-simple library
from rdio import Rdio
# and our example credentials
from rdio_consumer_credentials import RDIO_CREDENTIALS

# import web.py
import web

import urllib2

urls = (
  '/', 'root',
  '/login', 'login',
  '/callback', 'callback',
  '/logout', 'logout',
)
app = web.application(urls, globals())

class root:
  def GET(self):
    access_token = web.cookies().get('at')
    access_token_secret = web.cookies().get('ats')
    if access_token and access_token_secret:
      rdio = Rdio(RDIO_CREDENTIALS,
        (access_token, access_token_secret))
      # make sure that we can make an authenticated call

      try:
        currentUser = rdio.call('currentUser')['result']
      except urllib2.HTTPError:
        # this almost certainly means that authentication has been revoked for the app. log out.
        raise web.seeother('/logout')

      myPlaylists = rdio.call('getPlaylists')['result']['owned']

      response = '''
      <html><head><title>Rdio-Simple Example</title></head><body>
      <p>%s's playlists:</p>
      <ul>
      ''' % currentUser['firstName']
      for playlist in myPlaylists:
        response += '''<li><a href="%(shortUrl)s">%(name)s</a></li>''' % playlist
      response += '''</ul><a href="/logout">Log out of Rdio</a></body></html>'''
      return response
    else:
      return '''
      <html><head><title>Rdio-Simple Example</title></head><body>
      <a href="/login">Log into Rdio</a>
      </body></html>
      '''

class login:
  def GET(self):
    # clear all of our auth cookies
    web.setcookie('at', '', expires=-1)
    web.setcookie('ats', '', expires=-1)
    web.setcookie('rt', '', expires=-1)
    web.setcookie('rts', '', expires=-1)
    # begin the authentication process
    rdio = Rdio(RDIO_CREDENTIALS)
    url = rdio.begin_authentication(callback_url = web.ctx.homedomain+'/callback')
    # save our request token in cookies
    web.setcookie('rt', rdio.token[0], expires=60*60*24) # expires in one day
    web.setcookie('rts', rdio.token[1], expires=60*60*24) # expires in one day
    # go to Rdio to authenticate the app
    raise web.seeother(url)

class callback:
  def GET(self):
    # get the state from cookies and the query string
    request_token = web.cookies().get('rt')
    request_token_secret = web.cookies().get('rts')
    verifier = web.input()['oauth_verifier']
    # make sure we have everything we need
    if request_token and request_token_secret and verifier:
      # exchange the verifier and request token for an access token
      rdio = Rdio(RDIO_CREDENTIALS,
        (request_token, request_token_secret))
      rdio.complete_authentication(verifier)
      # save the access token in cookies (and discard the request token)
      web.setcookie('at', rdio.token[0], expires=60*60*24*14) # expires in two weeks
      web.setcookie('ats', rdio.token[1], expires=60*60*24*14) # expires in two weeks
      web.setcookie('rt', '', expires=-1)
      web.setcookie('rts', '', expires=-1)
      # go to the home page
      raise web.seeother('/')
    else:
      # we're missing something important
      raise web.seeother('/logout')
    
class logout:
  def GET(self):
    # clear all of our auth cookies
    web.setcookie('at', '', expires=-1)
    web.setcookie('ats', '', expires=-1)
    web.setcookie('rt', '', expires=-1)
    web.setcookie('rts', '', expires=-1)
    # and go to the homepage
    raise web.seeother('/')


if __name__ == "__main__":
    app.run()
