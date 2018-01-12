package com.xiao.zygeo.util;

import android.util.Log;

/**
 * Created by xiao on 2017/9/7.
 */

public class LogUtil  {
    public static void e(String msg){
        Log.e("zygeo-error",msg);
    }

    public static void v(String msg){
        Log.v("zygeo-verbose",msg);
    }
    public static void d(String msg){
        Log.d("zygeo-debug",msg);
    }
}
