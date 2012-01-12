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
using System.IO;
using System.Net;
using System.Collections.Generic;
using Rdio.Simple;
using Newtonsoft.Json.Linq;

namespace Rdio.Simple.Examples
{
	class CommandLine
	{
		public static void Main (string[] args)
		{
			RdioClient client = new RdioClient(RdioConsumerCredentials.Consumer);
		
			RdioClient.AuthState authState = client.BeginAuthentication("oob");
			Console.WriteLine ("Visit {0}", authState.Url);
			Console.Write ("Enter the PIN: ");
			string pin = Console.ReadLine ();
			OAuth.Token accessToken;
			try {
				accessToken = client.CompleteAuthentication(pin, authState.RequestToken);
			} catch (RdioClient.AuthorizationException ex) {
				Console.WriteLine (ex);
				return;
			}
			client = new RdioClient(client.Consumer, accessToken);
			
      JObject response = JObject.Parse(client.Call ("getPlaylists"));
      foreach(var playlist in response["result"]["owned"]) {
        Console.WriteLine("{0}\t{1}", playlist["shortUrl"], playlist["name"]);
      }
		}
	}
}
