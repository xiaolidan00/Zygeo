package com.xiao.zygeo.tool;

import android.content.Context;
import android.os.Environment;
import android.os.StrictMode;

import com.xiao.zygeo.util.ToastUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebData {

    public static String ROOT = "http://qxu1649270137.my3w.com/zygeo/";

    public static String LOGIN = ROOT + "user/user_login.php";
    public static String REDISTER = ROOT + "user/user_register.php";
    public static String FORGET = ROOT + "user/user_forget.php";
    public static String USER_SAVE = ROOT + "user/user_save.php";

    public static String COLLECT_LIST = ROOT + "record/collect_list.php";
    public static String COLLECT_IS = ROOT + "record/collect_is.php";
    public static String COLLECT_ON = ROOT + "record/collect_on.php";
    public static String COLLECT_OFF = ROOT + "record/collect_off.php";
    public static String RECORDLISTT = ROOT + "record/record_list.php";
    public static String RECORD_ADD = ROOT + "record/record_add.php";
    public static String RECORD_DETAIL = ROOT +"record/record_detail.php";
    public static String REMARK_ADD = ROOT +"record/remark_add.php";
    public static String REMARK_LIST = ROOT +"record/remark_list.php";

    public static String SEARCH_ZY = ROOT + "index/search_zy.php";
    public static String MAP = ROOT +"index/map.php";

    public static int RESULT_OK=0;
    public static int RESULT_FAIL=1;
    public static int SEARCH_ZY_CODE=1;
//    sha1加密
    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byteO = md[i];
                buf[k++] = hexDigits[byteO >>> 4 & 0xf];
                buf[k++] = hexDigits[byteO & 0xf];
            }
            return new String(buf);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
//网络线程问题
    public static void webHelper(){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
    }
//获取扩展名
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    public static boolean isEmail(String email) {
        Pattern emailPattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher matcher = emailPattern.matcher(email);
        if(matcher.find()){
            return true;
        }
        return false;
    }

}
