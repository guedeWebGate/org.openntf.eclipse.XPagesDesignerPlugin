package org.openntf.eclipse.xpdesigner.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openntf.eclipse.xpdesigner.core.publish.Instructions;

import com.google.gson.Gson;

public class PackagePublisher {

	private final String url;
	private final Instructions instructions;

	public PackagePublisher(String url, Instructions instructions) {
		super();
		this.url = url;
		this.instructions = instructions;
	}

	public void publish(ByteArrayOutputStream pkg, String username, String password) {
		HttpHost target = new HttpHost("cgu_srv9", 80, "http");
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope("cgu_srv9", 80),
				new UsernamePasswordCredentials(username, password));
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(target, basicAuth);

		// Add AuthCache to the execution context
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

		HttpPost uploadFile = new HttpPost(url);

		Gson gson = new Gson();
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody("instructions", gson.toJson(instructions), ContentType.APPLICATION_JSON);
		builder.addBinaryBody("package", pkg.toByteArray(), ContentType.DEFAULT_BINARY, "package.jar");
		HttpEntity multipart = builder.build();

		uploadFile.setEntity(multipart);

		CloseableHttpResponse response;
		try {
			response = httpClient.execute(target, uploadFile, localContext);
			HttpEntity responseEntity = response.getEntity();
			System.out.println(EntityUtils.toString(responseEntity));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
