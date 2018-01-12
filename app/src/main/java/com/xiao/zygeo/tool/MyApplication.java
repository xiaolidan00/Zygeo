package com.xiao.zygeo.tool;

import android.app.Application;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WebData.webHelper();
    }


}
