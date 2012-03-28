/*
 * (c) 2011-2012 Rdio Inc
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

@SuppressWarnings("UnusedDeclaration")
public abstract class RdioClient {
  protected final Consumer consumer;
  protected final Token accessToken;
  
  /**
   * Create a new Rdio client object without a token.
   * @param consumer the OAuth consumer
   */
  public RdioClient(Consumer consumer) {
    this.consumer = consumer;
    this.accessToken = null;
  }

  /**
   * Create a new Rdio client object with a token.
   * @param consumer    the OAuth consumer
   * @param accessToken the OAuth token
   */
  public RdioClient(Consumer consumer, Token accessToken) {
    this.consumer = consumer;
    this.accessToken = accessToken;
  }

  /**
   * Get the consumer
   * @return the consumer.
   */
  public Consumer getConsumer() {
    return consumer;
  }

  /**
   * Get the access token
   * @return the access token.
   */
  public Token getAccessToken() {
    return accessToken;
  }

  /**
   * Make an OAuth signed POST.
   * @param url            the URL to POST to
   * @param params         the parameters to post
   * @param token          the token to sign the call with
   * @return               the response body
   * @throws java.io.IOException   in the event of any network errors
   * @throws RdioException
   */
  protected abstract String signedPost(String url, Parameters params, Token token) throws IOException, AuthorizationException, RdioException;

  /**
   * Begin the authentication process. Fetch an OAuth request token associated with the supplied callback.
   * Store it on this Rdio object.
   * @param callback     the callback URL or "oob" for the PIN flow
   * @return             the request token and the authorization URL to direct a user to
   * @throws java.io.IOException in the event of any network errors
   */
  public AuthState beginAuthentication(String callback) throws IOException, RdioException {
    String response = signedPost("http://api.rdio.com/oauth/request_token",
        Parameters.build("oauth_callback", callback), null);
    Parameters parsed = Parameters.fromPercentEncoded(response);
    Token requestToken = new Token(parsed.get("oauth_token"), parsed.get("oauth_token_secret"));
    String url = parsed.get("login_url") + "?oauth_token=" + requestToken.token;
    return new AuthState(requestToken, url);
  }

  /**
   * Complete the authentication process. This Rdio object should have the request token from the beginAuthentication
   * method. When the authentication is complete the access token will be stored on this Rdio object.
   * @param verifier     the oauth_verifier from the callback or the PIN displayed to the user
   * @param requestToken the request token returned from the beginAuthentication call
   * @throws java.io.IOException in the event of any network errors
   * @return             the access token. pass it to an Rdio constructor to make authenticated calls
   */
  public Token completeAuthentication(String verifier, Token requestToken) throws IOException, RdioException {
    String response = signedPost("http://api.rdio.com/oauth/access_token",
        Parameters.build("oauth_verifier", verifier), requestToken);
    Parameters parsed = Parameters.fromPercentEncoded(response);
    return new Token(parsed.get("oauth_token"), parsed.get("oauth_token_secret"));
  }

  /**
   * Make and Rdio API call.
   * @param method       the name of the method
   * @param parameters   the parameters of the method
   * @return             the response JSON text
   * @throws java.io.IOException in the event of any network errors
   */
  public String call(String method, Parameters parameters) throws IOException, RdioException {
    parameters = (Parameters)parameters.clone();
    parameters.put("method", method);
    return signedPost("http://api.rdio.com/1/", parameters, accessToken);
  }

  /**
   * Make and Rdio API call with no parameters.
   * @param method       the name of the method
   * @return             the response JSON text
   * @throws java.io.IOException in the event of any network errors
   */
  public String call(String method) throws IOException, RdioException {
    return call(method, new Parameters());
  }


  /**
   * An OAuth Consumer key and secret pair.
   */
  public static class Consumer {
    public final String key;
    public final String secret;

    public Consumer(String key, String secret) {
      this.key = key;
      this.secret = secret;
    }
  }


  /**
   * An OAuth token and token secret pair.
   */
  public static final class Token {
    public final String token;
    public final String secret;
    
    public Token(String token, String secret) {
      this.token = token;
      this.secret = secret;
    }    
  }


  /**
   * Intermediate state for OAuth authorization.
   */
  public static final class AuthState {
    public final Token requestToken;
    public final String url;
    public AuthState(Token requestToken, String url) {
      this.requestToken = requestToken;
      this.url = url;
    }
  }
  
  
  public static class RdioException extends Exception {
	public RdioException(String error) {
      super(error);
    }
	private static final long serialVersionUID = -6660585967984993916L;
  }
  
  
  public static class AuthorizationException extends RdioException {
    public AuthorizationException(String error) {
      super(error);
    }
    private static final long serialVersionUID = 8748775513522136935L;		
  }
}
