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

using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Net;
using System.Web;
using System.Text;

namespace Rdio.Simple
{
	public class RdioClient
	{
		public class AuthState {
			readonly public OAuth.Token RequestToken;
			readonly public string Url;
			public AuthState(OAuth.Token requestToken, string url) {
				RequestToken = requestToken;
				Url = url;
			}
		}
		
		public class RdioException : Exception {
			public HttpWebRequest Request;
			public HttpWebResponse Response;
			public Exception Exception;
		}
		
		public class AuthorizationException : RdioException {
		}
		
		readonly public OAuth.Consumer Consumer;
		readonly public OAuth.Token AccessToken;
		
		public RdioClient (OAuth.Consumer consumer)
		{
			Consumer = consumer;
		}
		public RdioClient (OAuth.Consumer consumer, OAuth.Token accessToken)
		{
			Consumer = consumer;
			AccessToken = accessToken;
		}
		
		protected string SignedPost (string url, IDictionary<string,string> parameters, OAuth.Token token) {			
			HttpWebRequest request = (HttpWebRequest)WebRequest.Create (url);
			request.Method = "POST";
			request.ContentType = "application/x-www-form-urlencoded";
			request.Headers.Add ("Authorization", OAuth.Sign (Consumer, url, parameters, token, "POST", null));
			StreamWriter streamWriter = new StreamWriter (request.GetRequestStream ());
			streamWriter.Write (OAuth.PercentEscape (parameters));
			streamWriter.Close ();
			
			HttpWebResponse response;
			try {
				response = (HttpWebResponse)request.GetResponse ();
			} catch(WebException e) {
				RdioException re;
				response = (HttpWebResponse)e.Response;
				if (response.StatusCode == HttpStatusCode.Forbidden ||
				    response.StatusCode == HttpStatusCode.Unauthorized) {
					re = new AuthorizationException();
				} else {
					re = new RdioException();
				}
				re.Request = request;
				re.Response = response;
				re.Exception = e;
				throw re;
			}
			StreamReader streamReader = new StreamReader (response.GetResponseStream ());
			return streamReader.ReadToEnd ();
		}
		
		public string Call (string method, IDictionary<string,string> parameters) {
			var callParameters = new Dictionary<string,string>(parameters);
			callParameters.Add ("method", method);
			return SignedPost("http://api.rdio.com/1/", callParameters, AccessToken);
		}

		public string Call (string method) {
			return Call (method, new Dictionary<string,string>());
		}
		
		public AuthState BeginAuthentication(string callback) {
			string response = SignedPost("http://api.rdio.com/oauth/request_token", 
			                            new Dictionary<string,string> { { "oauth_callback", callback } }, 
			                            null);
			NameValueCollection parsed = HttpUtility.ParseQueryString(response);
			
			OAuth.Token requestToken = new OAuth.Token(parsed["oauth_token"], parsed["oauth_token_secret"]);
			string url = parsed["login_url"] + "?oauth_token=" + requestToken.Token_;
			return new AuthState(requestToken, url);
		}
		
		public OAuth.Token CompleteAuthentication(string verifier, OAuth.Token requestToken) {
			string response = SignedPost ("http://api.rdio.com/oauth/access_token",
			                              new Dictionary<string,string> { { "oauth_verifier", verifier } }, 
										  requestToken);
			NameValueCollection parsed = HttpUtility.ParseQueryString(response);
			return new OAuth.Token(parsed["oauth_token"], parsed["oauth_token_secret"]);
		}
	}
}

