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

package com.rdio.simple.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.*;

import com.rdio.simple.*;

public final class CommandLine {
  public static void main(String[] args) throws IOException, JSONException {
    ConsumerCredentials consumerCredentials = new ConsumerCredentials();
    RdioClient rdio = new RdioCoreClient(consumerCredentials);
    
    try {
      RdioClient.AuthState authState = rdio.beginAuthentication("oob");
      System.out.println("Go to: " + authState.url);
      System.out.print("Then enter the code: ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String verifier = reader.readLine().trim();
      RdioClient.Token accessToken = rdio.completeAuthentication(verifier, authState.requestToken);
      rdio = new RdioCoreClient(consumerCredentials, accessToken);
  
      try {
        JSONObject response = new JSONObject(rdio.call("getPlaylists"));
        JSONArray playlists = (JSONArray)((JSONObject)response.get("result")).get("owned");
        for (int i=0; i<playlists.length(); i++) {
          JSONObject playlist = playlists.getJSONObject(i);
          System.out.println(playlist.getString("shortUrl") + "\t" + playlist.getString("name"));
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
    } catch(RdioClient.RdioException e) {
      System.err.println("Rdio Error: "+e.toString());
    }
  }

}
