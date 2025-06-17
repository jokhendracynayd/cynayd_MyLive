package com.livestreaming.live.activity;

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

    public static void postRequest(String url, String jsonBody, Callback callback) {
        // Create the request body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Perform the request asynchronously
        client.newCall(request).enqueue(callback);
    }

    public static void callPost(String url, String jsonBody, String authToken, Resp resp) {

        postRequest(url, jsonBody, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("resChatPost", response.body().string());
                resp.onResponse(response.body().toString());
            }
        });
    }

    public static void callGet(String url, Resp resp) {

        getRequest(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (response!= null) {
                    resp.onResponse(response.body().string().toString());
                }
            }
        });
    }

    private static void getRequest(String url, Callback callback) {

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public interface Resp {
        public default void onResponse(String res){

        }
    }
}

