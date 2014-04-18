#!/usr/bin/env python
"""
run-tests - test oauth implementations in various languages

Language specific tester scripts take the following arguments:
  consumer key
  consumer secret
  url
  parameters (url encoded)
  token
  token secret
  method
  realm
  timestamp
  nonce
and return the resulting Authorization header value.
"""

import urllib, subprocess, time, random

testers = [('PHP', ['php', './tester.php']),
    ('Python', ['python', './tester.py']),
    ('Python3', ['python3', './tester.py']),
    ('Ruby', ['ruby', './tester.rb']),
    ('Ruby 1.9', ['ruby-1.9', './tester.rb']),
    ('Node.js', ['node', './tester.node']),
    ('Java', ['java', '-classpath', '../java/classes:.', 'Tester'])]

def test(consumer,
         url,
         params,
         token = None,
         method = 'POST',
         realm = None,
         timestamp = None,
         nonce = None):
  args = [consumer[0], consumer[1],
          url, urllib.urlencode(params),
          token[0] if token is not None else '',
          token[1] if token is not None else '',
          method,
          realm if realm is not None else '',
          timestamp if timestamp is not None else str(int(time.time())),
          nonce if nonce is not None else str(random.randint(0, 1000000))]
  results = []
  for language, command in testers:
    try:
      results.append((language, subprocess.check_output(command + args)))
    except Exception as e:
      results.append((language, 'Error encountered: %s' % repr(e)))

  if len(set(result for language, result in results)) > 1:
    print 'ERROR:'
    print '%s %s' % (method, url)
    print '%r' % params
    print 'consumer=%r token=%r' % (consumer, token)
    print 'realm=%r' % realm
    for language, result in results:
      print '%10s %s' % (language, result)
  else:
    print 'OK'



test(('a','b'), 'http://www.example.com/', {})
test(('a','b'), 'Http://www.example.com/', {})
test(('a','b'), 'http://www.EXAMPLE.com/', {})
test(('a','b'), 'http://www.example.com/~test/', {})
test(('a','b'), 'http://www.example.com/'+urllib.quote(u'fran\xe7ais'.encode('UTF-8')), {})
test(('a','b'), 'http://www.example.com/', {'test':'thing', 'other':'test'})
test(('a','b'), 'http://www.example.com/', {'extras':'-*,names', 'foo':'-._~%+&=I what'})
test(('a','b'), 'http://www.example.com/', {'test':'thing', 'TEST':'foo'})
test(('a','b'), 'http://www.example.com/', {'language':u'Fran\xe7ais'.encode('UTF-8')})
test(('a','b'), 'http://www.example.com/', {},
    token=('to','ken'))
test(('a','b'), 'http://www.example.com/', {'test':'thing', 'other':'test'},
    token=('to','ken'))
test(('a','b'), 'https://www.example.com/', {'test':'thing', 'other':'test'},
    token=('to','ken'))
test(('a','b'), 'http://www.example.com/', {'oauth_callback':'https://www.example.com/my/callback'})
test(('a','b'), 'http://www.example.com/?foo=bar', {'test':'thing', 'other':'test'})
