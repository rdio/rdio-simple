/*
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

package com.rdio.simple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Rdio {
  public String consumerKey;
  public String consumerSecret;
  public String token;
  public String tokenSecret;

  /**
   * Create a new Rdio client object without a token.
   * @param consumerKey    the OAuth consumer key
   * @param consumerSecret the OAuth consumer secret
   */
  public Rdio(String consumerKey, String consumerSecret) {
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
  }

  /**
   * Create a new Rdio client object with a token.
   * @param consumerKey    the OAuth consumer key
   * @param consumerSecret the OAuth consumer secret
   * @param token          the OAuth token
   * @param tokenSecret    the OAuth token secret
   */
  public Rdio(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.token = token;
    this.tokenSecret = tokenSecret;
  }

  /**
   * Make an OAuth signed POST.
   * @param url          the URL to POST to
   * @param params       the parameters to post
   * @return             the response body
   * @throws IOException in the event of any network errors
   */
  private String signedPost(String url, Parameters params) throws IOException {
    String auth = Om.sign(consumerKey, consumerSecret, url, params, token, tokenSecret, "POST", null);

    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", auth);
      connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
      connection.setDoOutput(true);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(params.toPercentEncoded());
      writer.close();
      InputStreamReader reader = new InputStreamReader(connection.getInputStream());
      int length = connection.getContentLength();
      char[] chars = new char[length];
      int offset = 0;
      int count;
      do {
        count = reader.read(chars, offset, length-offset);
        offset += count;
      } while(count > 0 && offset < length);
      return new String(chars);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Begin the authentication process. Fetch an OAuth request token associated with the supplied callback.
   * Store it on this Rdio object.
   * @param callback     the callback URL or "oob" for the PIN flow
   * @return             the authorization URL to direct a user to
   * @throws IOException in the event of any network errors
   */
  public String beginAuthentication(String callback) throws IOException {
    String response = signedPost("http://api.rdio.com/oauth/request_token",
        Parameters.build("oauth_callback", callback));
    Parameters parsed = Parameters.fromPercentEncoded(response);
    token = parsed.get("oauth_token");
    tokenSecret = parsed.get("oauth_token_secret");

    return parsed.get("login_url") + "?oauth_token=" + parsed.get("oauth_token");
  }

  /**
   * Complete the authentication process. This Rdio object should have the request token from the beginAuthentication
   * method. When the authentication is complete the access token will be stored on this Rdio object.
   * @param verifier     the oauth_verifier from the callback or the PIN displayed to the user
   * @throws IOException in the event of any network errors
   */
  public void completeAuthentication(String verifier) throws IOException {
    String response = signedPost("http://api.rdio.com/oauth/access_token",
        Parameters.build("oauth_verifier", verifier));
    Parameters parsed = Parameters.fromPercentEncoded(response);
    token = parsed.get("oauth_token");
    tokenSecret = parsed.get("oauth_token_secret");
  }

  /**
   * Make and Rdio API call.
   * @param method       the name of the method
   * @param parameters   the parameters of the method
   * @return             the response JSON text
   * @throws IOException in the event of any network errors
   */
  public String call(String method, Parameters parameters) throws IOException {
    parameters = (Parameters)parameters.clone();
    parameters.put("method", method);
    return signedPost("http://api.rdio.com/1/", parameters);
  }

  /**
   * Make and Rdio API call with no parameters.
   * @param method       the name of the method
   * @return             the response JSON text
   * @throws IOException in the event of any network errors
   */
  public String call(String method) throws IOException {
    return call(method, new Parameters());
  }
}
