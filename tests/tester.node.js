om = require('./../node/om');
args = process.argv.slice(2);
consumer = [args[0], args[1]];
url = args[2];
params = require('querystring').parse(args[3]);
if (args[4] != '' && args[5] != '') {
  token = [args[4], args[5]];
} else {
  token = null;
}
method = args[6];
if (args[7] != '') {
  realm = args[7];
} else {
  realm = null;
}
timestamp = args[8];
nonce = args[9];
process.stdout.write(om(consumer, url, params, token, method, realm, timestamp, nonce));
