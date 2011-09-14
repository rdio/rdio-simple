#!/usr/bin/php -q
<?
require('../php/om.php');

$consumer = array($argv[1], $argv[2]);
$url = $argv[3];
$params = array();
parse_str($argv[4], $params);
if ($argv[5] != '' && $argv[6] != '') {
  $token = array($argv[5], $argv[6]);
} else {
  $token = NULL;
}
$method = $argv[7];
if ($argv[8] != '') {
  $realm = $argv[8];
} else {
  $realm = NULL;
}
$timestamp = $argv[9];
$nonce = $argv[10];
print om($consumer, $url, $params, $token, $method, $realm, $timestamp, $nonce);
?>