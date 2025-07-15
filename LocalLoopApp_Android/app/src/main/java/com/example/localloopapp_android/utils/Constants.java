package com.example.localloopapp_android.utils;

import com.example.localloopapp_android.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Constants {
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_FIRST_NAME = "firstName";
    public static final String EXTRA_LAST_NAME = "lastName";



    // event management extras
    public static final String EXTRA_EVENT_OBJECT = "event";

    public static String formatDate(long epochMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(epochMillis));
    }

}
