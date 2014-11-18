/**
 *
 */
package edu.maryland.leafsnap.api;

import android.os.Looper;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.client.params.ClientPNames;

/**
 * @author Arthur Jacobs
 */
public class LeafletRestClient {
    private static final String BASE_URL = "http://api.leafsnap.com/v1";

    private static AsyncHttpClient client;

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler, boolean encodeURL) {
        getClient().setURLEncodingEnabled(encodeURL);
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static AsyncHttpClient getClient() {
        if (client == null) {
            if (Looper.myLooper() == null) {
                client = new SyncHttpClient();
            }
            else {
                client = new AsyncHttpClient();
            }
        }
        client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        return client;
    }
}
