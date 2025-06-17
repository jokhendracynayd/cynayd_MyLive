package com.livestreaming.main.utils;

import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;


public class NetworkUtils {

    private static final OkHttpClient client = new OkHttpClient();

    public static void postRequest(String url, String jsonBody, String authToken, Callback callback) {
        // Create the request body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        // Perform the request asynchronously
        client.newCall(request).enqueue(callback);
    }

    public static void postRequest2(String url, String jsonBody, String authToken, Callback callback) {
        // Create the request body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        // Perform the request asynchronously
        client.newCall(request).enqueue(callback);
    }

    public static void callPost(String url, String jsonBody, String authToken, Resp resp) {

        postRequest(url, jsonBody, authToken, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("resChatPost", response.body().string());
                resp.onResponse(response.body().string());
            }
        });
    }

    public static void callPost2(String url, String jsonBody, String authToken, Resp resp) {
        postRequest2(url, jsonBody, authToken, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("resChatPost", response.body().string());
                resp.onResponse(response.body().string());
            }
        });
    }

    public interface Resp {
        public void onResponse(String res);
    }
}

