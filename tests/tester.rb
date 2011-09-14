#!/usr/bin/env ruby

$LOAD_PATH << '../ruby'

require 'rubygems'
require 'om'
require 'cgi'

consumer = [ARGV[0], ARGV[1]]
url = ARGV[2]
params = CGI.parse(ARGV[3])
params.each_pair{|k,v| params[k] = v[0]}
if ARGV[4] != '' and ARGV[5]
  token = [ARGV[4], ARGV[5]]
else
  token = nil
end
method = ARGV[6]
if ARGV[7] != ''
  realm = ARGV[7]
else
  realm = nil
end
timestamp = ARGV[8]
nonce = ARGV[9]
print om(consumer, url, params, token, method, realm, timestamp, nonce)
