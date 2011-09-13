#!/usr/bin/env python

import sys,os.path, urlparse, sys
sys.path.insert(0, os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'python'))
from om import om

consumer = (sys.argv[1], sys.argv[2])
url = sys.argv[3]
params = urlparse.parse_qsl(sys.argv[4])
if sys.argv[5] != '' and sys.argv[6] != '':
  token = (sys.argv[5], sys.argv[6])
else:
  token = None
method = sys.argv[7]
if sys.argv[8] != '':
  realm = sys.argv[8]
else:
  realm = None
timestamp = sys.argv[9]
nonce = sys.argv[10]
sys.stdout.write(om(consumer, url, params, token, method, realm, timestamp, nonce))
