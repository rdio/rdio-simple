package com.rdio.simple;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * An RdioClient that uses java.net.HttpURLConnection.
 */
public class RdioCoreClient extends RdioClient {

	public RdioCoreClient(Consumer consumer) {
		super(consumer);
	}

	public RdioCoreClient(Consumer consumer, Token accessToken) {
		super(consumer, accessToken);
	}

	/**
	 * Make an OAuth signed POST.
	 * @param url            the URL to POST to
	 * @param params         the parameters to post
	 * @param token          the token to sign the call with
	 * @return               the response body
	 * @throws java.io.IOException   in the event of any network errors
	 * @throws com.rdio.simple.RdioClient.RdioException
	 */
	protected String signedPost(String url, Parameters params, Token token) throws IOException, AuthorizationException, RdioException {
		String auth;
		if (token == null) {
			auth = Om.sign(consumer.key, consumer.secret, url, params, null, null, "POST", null);
		} else {
			auth = Om.sign(consumer.key, consumer.secret, url, params, token.token, token.secret, "POST", null);
		}

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Authorization", auth);
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			String postBody = params.toPercentEncoded();
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(postBody);
			writer.close();
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw new AuthorizationException(connection.getResponseMessage());
			} else if (responseCode < 200 || responseCode >= 300) {
				throw new RdioException("Unexpected response: "+responseCode+" "+connection.getResponseMessage());
			}
			int length = connection.getContentLength();
                        InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			char[] chars = new char[length];
			int offset = 0;
			int count;
			do {
				count = reader.read(chars, offset, length-offset);
				offset += count;
			} while(count > 0 && offset < length);
			return new String(chars);
		} catch (MalformedURLException e) {
			throw new RdioException(e.toString());
		}
	}


}
