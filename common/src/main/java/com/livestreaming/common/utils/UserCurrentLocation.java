package com.livestreaming.common.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

public class UserCurrentLocation {
     static String latitude;
     static String longitude;
    private static String exclude = "weekly";
    private static String units = "metric";
    private static String language = "en";


    public static void setLatitude(String latitude) {
        UserCurrentLocation.latitude = latitude;
    }

    public static String getLongitude() {
        return longitude;
    }

    public static void setLongitude(String longitude) {
        UserCurrentLocation.longitude = longitude;
    }

    public static String getExclude() {
        return exclude;
    }

    public static void setExclude(String exclude) {
        UserCurrentLocation.exclude = exclude;
    }

    public static String getUnits() {
        return units;
    }

    public static void setUnits(String units) {
        UserCurrentLocation.units = units;
    }

    public static String getLanguage() {
        return language;
    }

    public static Address getAddress(Context context) {
        List<Address> address = null;
        Geocoder geocoder = new Geocoder(context, new Locale(LanguageUtil.getInstance().getLanguage()));

        try {
            if (latitude != null && !latitude.isEmpty()) {

                address = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         if(address!=null&&!address.isEmpty()) {
             return   address.get(0);
        }else{
             return   null;
        }
    }

    public static LatLng getLatLonForAddress(String location, Context context) {
        Geocoder geocoder = new Geocoder(context, new Locale(LanguageUtil.getInstance().getLanguage()));
        List<Address> addressList = null;

        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (addressList != null && !addressList.isEmpty()) {
            Address address = addressList.get(0);
            return new LatLng(address.getLatitude(), address.getLongitude());
        }

        return new LatLng(0.0, 0.0);
    }
}

