/**
 *
 */
package edu.maryland.leafsnap.api;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

/**
 * @author Arthur Jacobs
 */
public class LeafletSyncRestClient {
    private static final String BASE_URL = "http://api.leafsnap.com/v1";

    private static SyncHttpClient client;

    public static void get(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        getClient().get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        getClient().post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static SyncHttpClient getClient() {
        if (client == null) {
            client = new SyncHttpClient();
        }
        return client;
    }
}
