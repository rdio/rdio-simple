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
using System.Web;
using System.Security.Cryptography;
using System.Text;

namespace Rdio.Simple
{
	public abstract class OAuth
	{
		public class Consumer {
			private string key, secret;
			public string Key { get { return key; } }
			public string Secret { get { return secret; } }
			public Consumer(string key, string secret) {
				this.key = key;
				this.secret = secret;
			}
		}
		
		public class Token {
			private string token, secret;
			public string Token_ { get { return token; } }
			public string Secret { get { return secret; } }
			public Token(string token, string secret) {
				this.token = token;
				this.secret = secret;
			}
		}
		
		public static string Sign(Consumer consumer,
		                          string url,
		                          IDictionary<string,string> postParams,
		                          Token token,
		                          string method,
		                          string realm) {
			RNGCryptoServiceProvider random = new RNGCryptoServiceProvider();
			byte[] nonce = new byte[4];
			random.GetBytes (nonce);
			UInt64 timestamp = Convert.ToUInt64((DateTime.UtcNow - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalSeconds);
			return Sign (consumer, url, postParams, token, method, realm,
			             timestamp.ToString(),
			             String.Format ("{0:X}{1:X}{2:X}{3:X}", nonce[0], nonce[1], nonce[2], nonce[3]));
		}
		
		public static string Sign(Consumer consumer,
		                          string url, IDictionary<string,string> postParams,
		                          Token token,
		                          string method, string realm,
		                          string timestamp, string nonce) {
			// copy the parameters into a sorted dictionary
			var sortedParams = new SortedDictionary<string, string>(postParams);
			
			// method is upper-case, defaults to POST
			if (method == null) {
				method = "POST";
			} else {
				method = method.ToUpper();
			}
			
			// normalize the URL
			UriBuilder uribuilder = new UriBuilder(url);
			uribuilder.Scheme = uribuilder.Scheme.ToLower();
			if ((uribuilder.Scheme == "http" && uribuilder.Port == 80) ||
			    (uribuilder.Scheme == "https" && uribuilder.Port == 443)) {
				uribuilder.Port = -1;
			}
			string normalizedUrl = uribuilder.ToString();
			
			// add the query-string parameters
			if (uribuilder.Query != null) {
				foreach (KeyValuePair<string,string> kvp in HttpUtility.ParseQueryString(uribuilder.Query)) {
					sortedParams.Add (kvp.Key, kvp.Value);
				}
			}
			
			// build the OAuth parameters
			var oauthParams = new Dictionary<string,string> {
				{"oauth_version", "1.0"},
				{"oauth_timestamp", timestamp},
				{"oauth_nonce", nonce},
				{"oauth_signature_method", "HMAC-SHA1"},
				{"oauth_consumer_key", consumer.Key},
			};
			
			// build the HMAC key
			string hmacKey = consumer.Secret + "&";
			if (token != null) {
				hmacKey += token.Secret;
				// and add the token to the params
				oauthParams.Add ("oauth_token", token.Token_);
			}
			
			// add the OAuth parameters
			foreach (KeyValuePair<string,string> kvp in oauthParams) {
				sortedParams.Add (kvp.Key, kvp.Value);
			}
			
			// escape the key/value pairs into normalized parameters
			string normalizedParams = PercentEscape(sortedParams);
			
			// build the signature base string
			string signatureBaseString = Uri.EscapeDataString(method) + '&' + Uri.EscapeDataString(normalizedUrl) + '&' + Uri.EscapeDataString(normalizedParams);
			
			// calculate the HMAC SHA1
			Encoding utf8 = new UTF8Encoding();
			HMACSHA1 hmac = new HMACSHA1(utf8.GetBytes(hmacKey));
			byte[] oauthSignature = hmac.ComputeHash(utf8.GetBytes (signatureBaseString));
			
			
			// build the authorization header
			string header = "OAuth ";
			// add the realm
			if (realm != null) {
				header += "realm=\"" + realm + "\", ";
			}
			// add the signature
			header += "oauth_signature=\"" + Convert.ToBase64String(oauthSignature) + '"';
			// add the oauth parameters
			foreach (string key in oauthParams.Keys) {
				header += ", " + key + "=\"" + sortedParams[key] + '"';
			}
			return header;
		}

		public static string PercentEscape(IDictionary<string,string> parm) {
			string result = "";
			foreach(KeyValuePair<string,string> kvp in parm) {
				if (result.Length > 0) {
					result = result + '&';
				}
				result = result + Uri.EscapeDataString(kvp.Key) + '=' + Uri.EscapeDataString(kvp.Value);
			}
			return result;
		}
	}
}

