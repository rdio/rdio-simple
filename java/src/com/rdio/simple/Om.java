package com.rdio.simple;
/*
 * om is oauth-mini - a simple implementation of a useful subset of OAuth.
 * It's designed to be useful and reusable but not general purpose.
 *
 * (c) 2011 Rdio Inc
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;

/**
 * A straight-forward implementation of OAuth 1.0a message signing.
 */
public final class Om {
  /**
   * Sign an OAuth request.
   * @param consumerKey    the OAuth consumer key
   * @param consumerSecret the OAuth consumer secret
   * @param url            the URL of the OAuth endpoint
   * @param postParams     the parameters that will be POSTed
   * @param token          the OAuth token (can be null)
   * @param tokenSecret    the OAuth token secret (can be null)
   * @param method         the HTTP method (can be null, defaults to "POST")
   * @param realm          the realm for the Authorization header
   * @return               an OAuth Authorization header
   */
  public static String sign(String consumerKey, String consumerSecret,
                            String url, Parameters postParams,
                            String token, String tokenSecret,
                            String method, String realm) {
    return sign(consumerKey, consumerSecret, url, postParams, token, tokenSecret, method, realm,
        "" + Math.round(Calendar.getInstance().getTimeInMillis() / 1000),
        "" + (new Random()).nextInt(1000000));
  }

  /**
   * Sign an OAuth request.
   * @param consumerKey    the OAuth consumer key
   * @param consumerSecret the OAuth consumer secret
   * @param url            the URL of the OAuth endpoint
   * @param postParams     the parameters that will be POSTed
   * @param token          the OAuth token (can be null)
   * @param tokenSecret    the OAuth token secret (can be null)
   * @param method         the HTTP method (can be null, defaults to "POST")
   * @param realm          the realm for the Authorization header
   * @param timestamp      the OAuth timestamp (can be null, defaults to current time)
   * @param nonce          the OAuth nonce (can be null, auto generated)
   * @return               an OAuth Authorization header
   */
  public static String sign(String consumerKey, String consumerSecret,
                            String url, Parameters postParams,
                            String token, String tokenSecret,
                            String method, String realm,
                            String timestamp, String nonce) {
    // copy the parameters into a sorted map
    Parameters params = (Parameters)postParams.clone();

    // method defaults to POST and must be upper-case
    if (method == null) {
      method = "POST";
    } else {
      method = method.toUpperCase();
    }

    // normalize the URL
    URL uri;
    try {
      uri = new URL(url);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return "";
    }
    String scheme = uri.getProtocol().toLowerCase();
    int port = uri.getPort();
    // leave off default ports
    if ((scheme.equals("http") && port == 80) ||
        (scheme.equals("https") && port == 443)) {
      port = -1;
    }
    String normalized_url = scheme + "://" + uri.getHost().toLowerCase() + (port == -1 ? "" : (":" + port)) + uri.getPath();

    // add the query-string parameters
    if (uri.getQuery() != null && uri.getQuery().length() > 0) {
      params.putAll(Parameters.fromPercentEncoded(uri.getQuery()));
    }

    // add the OAuth parameters
    params.put("oauth_version", "1.0");
    params.put("oauth_timestamp", timestamp);
    params.put("oauth_nonce", nonce);
    params.put("oauth_signature_method", "HMAC-SHA1");
    params.put("oauth_consumer_key", consumerKey);

    // the HMAC key
    String hmac_key = consumerSecret + "&";
    if (token != null && tokenSecret != null) {
      hmac_key = hmac_key + tokenSecret;
      params.put("oauth_token", token);
    }

    // escape the key/value pairs into normalized parameters
    String normalized_params = params.toPercentEncoded();

    // build the signature base string
    String signature_base_string = Parameters.percentEncode(method) + '&' +
        Parameters.percentEncode(normalized_url) + '&' +
        Parameters.percentEncode(normalized_params);

    // calculate the HMAC SHA1
    String oauth_signature = HMAC_SHA1(hmac_key, signature_base_string);

    // build the authorization header
    StringBuilder header = new StringBuilder("OAuth ");
    // add the realm
    if (realm != null) {
      header.append("realm=\"");
      header.append(realm);
      header.append("\", ");
    }
    // add the signature
    header.append("oauth_signature=\"");
    header.append(oauth_signature);
    header.append("\", ");
    
    // grab header parameters
    Parameters header_params = params.filter(
        new Parameters.Filter() {
          @Override
          public boolean filter(String key, String value) {
            return headerParameters.contains(key);
          }
        });
    header.append(header_params.toHeader());

    return header.toString();
  }

  private static final HashSet<String> headerParameters;

  static {
    headerParameters = new HashSet<String>(7);
    headerParameters.add("oauth_version");
    headerParameters.add("oauth_timestamp");
    headerParameters.add("oauth_nonce");
    headerParameters.add("oauth_signature_method");
    headerParameters.add("oauth_consumer_key");
    headerParameters.add("oauth_token");
  }

  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

  private static String HMAC_SHA1(String key, String text) {
    SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

    Mac mac;
    try {
      mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return "";
    } catch (InvalidKeyException e) {
      e.printStackTrace();
      return "";
    }

    return new String(base64(mac.doFinal(text.getBytes())));
  }

  // From: http://www.source-code.biz/base64coder/java/Base64Coder.java.txt
  // slightly modified
  // Mapping table from 6-bit nibbles to Base64 characters.
  private static final char[] base64_map = new char[64];

  static {
    int i = 0;
    for (char c = 'A'; c <= 'Z'; c++) base64_map[i++] = c;
    for (char c = 'a'; c <= 'z'; c++) base64_map[i++] = c;
    for (char c = '0'; c <= '9'; c++) base64_map[i++] = c;
    base64_map[i++] = '+';
    base64_map[i] = '/';
  }

  /**
   * Encode bytes in Base 64.
   * Based on:
   * http://www.source-code.biz/base64coder/java/Base64Coder.java.txt
   *
   * @param in the bytes to encode
   * @return Base 64 encoded characters
   */
  public static char[] base64(byte[] in) {
    int iLen = in.length;
    int oDataLen = (iLen * 4 + 2) / 3;       // output length without padding
    int oLen = ((iLen + 2) / 3) * 4;         // output length including padding
    char[] out = new char[oLen];
    int ip = 0;
    int op = 0;
    while (ip < iLen) {
      int i0 = in[ip++] & 0xff;
      int i1 = ip < iLen ? in[ip++] & 0xff : 0;
      int i2 = ip < iLen ? in[ip++] & 0xff : 0;
      int o0 = i0 >>> 2;
      int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
      int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
      int o3 = i2 & 0x3F;
      out[op++] = base64_map[o0];
      out[op++] = base64_map[o1];
      out[op] = op < oDataLen ? base64_map[o2] : '=';
      op++;
      out[op] = op < oDataLen ? base64_map[o3] : '=';
      op++;
    }
    return out;
  }

  /* no, you can't construct this */
  private Om() { }
}
