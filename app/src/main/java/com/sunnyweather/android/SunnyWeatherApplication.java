package com.sunnyweather.android;

import android.app.Application;
import android.content.Context;

public class SunnyWeatherApplication extends Application {
    private static Context context;
    public static final String TOKEN = "11uRyhSghq8ni5zR";//申请到的令牌值
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }

}
