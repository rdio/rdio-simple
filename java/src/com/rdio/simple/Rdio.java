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

@SuppressWarnings("UnusedDeclaration")
public class Rdio {
  private Consumer consumer;
  private Token token;
  
  /**
   * Create a new Rdio client object without a token.
   * @param consumer the OAuth consumer
   */
  public Rdio(Consumer consumer) {
    this.consumer = consumer;
  }

  /**
   * Create a new Rdio client object with a token.
   * @param consumer the OAuth consumer
   * @param token    the OAuth token
   */
  public Rdio(Consumer consumer, Token token) {
    this.consumer = consumer;
    this.token = token;
  }

  /**
   * Get the consumer
   * @return the consumer.
   */
  public synchronized Consumer getConsumer() {
    return consumer;
  }

  /**
   * Get the token
   * @return the token.
   */
  public synchronized Token getToken() {
    return token;
  }

  /**
   * Set the consumer
   * @param consumer the consumer.
   */
  public synchronized void setConsumer(Consumer consumer) {
    this.consumer = consumer;
  }

  /**
   * Set the token
   * @param token the token.
   */
  public synchronized void setToken(Token token) {
    this.token = token;
  }

  /**
   * Make an OAuth signed POST.
   * @param url          the URL to POST to
   * @param params       the parameters to post
   * @return             the response body
   * @throws IOException in the event of any network errors
   */
  private String signedPost(String url, Parameters params) throws IOException {
    String auth;
    synchronized(this) {
      auth = Om.sign(consumer.key, consumer.secret, url, params,
              (token == null)?null:token.token, (token == null)?null:token.secret, "POST", null);
    }

    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Authorization", auth);
      connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
      connection.setDoOutput(true);
      String postBody = params.toPercentEncoded();
      connection = modifyConnection(connection, url, params);
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(postBody);
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
   * Modify the HttpURLConnection for a signedPost.
   * The default implementation does nothing.
   * @param connection the HttpURLConnection
   * @param url the URL being requested
   * @param params the parameters being passed
   * @return the modified HttpURLConnection
   */
  @SuppressWarnings("UnusedParameters")
  protected HttpURLConnection modifyConnection(HttpURLConnection connection, String url, Parameters params) {
    return connection;
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
    String url;
    synchronized (this) {
      token = new Token(parsed.get("oauth_token"), parsed.get("oauth_token_secret"));
      url = parsed.get("login_url") + "?oauth_token=" + token.token;
    }
    return url;
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
    synchronized(this) {
      token = new Token(parsed.get("oauth_token"), parsed.get("oauth_token_secret"));
    }
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


  /**
   * An OAuth Consumer key and secret pair.
   */
  public static class Consumer {
    public String key;
    public String secret;

    public Consumer(String key, String secret) {
      this.key = key;
      this.secret = secret;
    }
  }

  /**
   * An OAuth token and token secret pair.
   */
  public static class Token {
    public String token;
    public String secret;
    
    public Token(String token, String secret) {
      this.token = token;
      this.secret = secret;
    }    
  }

}
