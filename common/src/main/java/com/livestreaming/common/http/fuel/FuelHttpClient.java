package com.livestreaming.common.http.fuel;

import com.lzy.okgo.https.HttpsUtils;
import com.livestreaming.common.CommonAppConfig;
import com.livestreaming.common.http.IHttpClient;
import com.livestreaming.common.http.IHttpRequest;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by cxf on 2018/9/17.
 */

public class FuelHttpClient implements IHttpClient {

    private final String mUrl;
//    private final HashMap<String, FuelRequest> mMap;


    public FuelHttpClient() {
        mUrl = CommonAppConfig.HOST + "/appapi/";
//        mMap = new HashMap<>();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession sslsession) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslParams.sSLSocketFactory);
    }

    public IHttpRequest get(String serviceName, String tag) {
        FuelRequest req = new FuelRequest(mUrl, FuelRequest.Method.GET);
        req.setServiceName(serviceName);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }


    public IHttpRequest post(String serviceName, String tag) {
        FuelRequest req = new FuelRequest(mUrl, FuelRequest.Method.POST);
        req.setServiceName(serviceName);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    @Override
    public IHttpRequest getString(String url, String tag) {
        FuelRequest req = new FuelRequest(url, FuelRequest.Method.GET);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    @Override
    public IHttpRequest getFile(String url, String tag) {
        FuelRequest req = new FuelRequest(url, FuelRequest.Method.GET);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    public void cancel(String tag) {
//        if (!TextUtils.isEmpty(tag)) {
//            FuelRequest req = mMap.get(tag);
//            if (req != null) {
//                req.cancel();
//                mMap.remove(tag);
//            }
//        }
    }

}
