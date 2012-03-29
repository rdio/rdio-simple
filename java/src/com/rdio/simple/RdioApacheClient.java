package com.rdio.simple;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * An RdioClient that uses the Apache HttpClient.
 *
 * To customize the HttpClient to be used subclass RdioApacheClient and
 * override signedPost(String url, Parameters params, RdioClient.Token token)
 * to call signedPost(String url, Parameters params, RdioClient.Token token, HttpClient client)
 * with a specific HttpClient.
 */
public class RdioApacheClient extends RdioClient {
	public RdioApacheClient(RdioClient.Consumer consumer) {
		super(consumer);
	}
	public RdioApacheClient(RdioClient.Consumer consumer, RdioClient.Token accessToken) {
		super(consumer, accessToken);
	}
	
	@Override
	protected String signedPost(String url, Parameters params, RdioClient.Token token) throws IOException, RdioException {
		return signedPost(url, params, token, new DefaultHttpClient());
	}

        /** Make a signed POST with a specific HttpClient.
         * @param url            the URL to POST to
         * @param params         the parameters to post
         * @param token          the token to sign the call with
         * @param client         the HttpClient to use
         * @return               the response body
         * @throws IOException   in the event of any network errors
         * @throws RdioException in the event of an Rdio protocol error
         */
	protected String signedPost(String url, Parameters params, RdioClient.Token token, HttpClient client) throws IOException, RdioException {	
	    String auth;
	    if (token == null) {
	      auth = Om.sign(consumer.key, consumer.secret, url, params, null, null, "POST", null);
	    } else {
	      auth = Om.sign(consumer.key, consumer.secret, url, params, token.token, token.secret, "POST", null);
	    }
		HttpPost post = new HttpPost(url);

		post.setEntity(new StringEntity(params.toPercentEncoded()));
		post.addHeader("Cache-Control", "no-transform");
		post.addHeader("Authorization", auth);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");

		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			String error = "Request failed with status " + response.getStatusLine();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
				throw new AuthorizationException(error);
			}
			throw new RdioException(error);
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			String error = "Null entity in response";
			throw new RdioException(error); 
		}
		return EntityUtils.toString(entity);
	}
	
}
