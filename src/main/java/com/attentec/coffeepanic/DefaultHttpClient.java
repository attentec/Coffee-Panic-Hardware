package com.attentec.coffeepanic;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class DefaultHttpClient implements HttpClient {
    private final String serverUrl;
    private CloseableHttpClient client;
    private HttpClientContext context;

    public DefaultHttpClient(String serverUrl, Credentials credentials) {
        this.serverUrl = serverUrl;

        String username = credentials.getUsername();
        String password = credentials.getPassword();

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        AuthCache cache = new BasicAuthCache();
        cache.put(HttpHost.create(serverUrl), new BasicScheme());

        client = HttpClientBuilder.create().build();
        context = HttpClientContext.create();
        context.setCredentialsProvider(provider);
        context.setAuthCache(cache);
    }

    public void close() throws HttpException {
        try {
            client.close();
            client = null;
        } catch (IOException e) {
            throw new HttpException("Failed to close client", e);
        }
    }

    public void postMeasurement(float grams) throws HttpException {
        post("/api/measurement", "{\"valueInGrams\":" + grams + "}");
    }

    private void post(String path, String json) throws HttpException {
        String url = serverUrl + path;
        HttpPost request = new HttpPost(url);

        try {
            request.addHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(json));

            try (CloseableHttpResponse response = client.execute(request, context)) {
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                if (statusCode != 200) {
                    throw new HttpException("Failed to post to " + url +  ", HTTP status code " + statusCode);
                }
            }
        } catch (IOException e) {
            throw new HttpException("Failed to post to " + url, e);
        }
    }
}
