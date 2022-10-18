package com.mickstarify.zotero;

import android.util.Log;

/**
 * @version V1.0
 * @Author : Moyear
 * @Time : 2022/10/18 23:22
 * @Description : 应用日志类
 */
public class MyLog {

    private static MyLog INSTANCE;

    public static int VERBOSE = 1;
    public static int DEBUG = 2;
    public static int INFO = 3;
    public static int WARN = 4;
    public static int ERROR = 5;
    public static int NOTHING = 6;

    public static int LEVEL = VERBOSE;

    private MyLog() {}

    public static MyLog init() {
        if (INSTANCE == null) {
            synchronized (MyLog.class) {
                INSTANCE = new MyLog();
            }
        }
        return INSTANCE;
    }

    public static void v(String tag, String msg) {
        if (LEVEL <= VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (LEVEL <= DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (LEVEL <= INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (LEVEL <= WARN) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= ERROR) {
            Log.e(tag, msg);
        }
    }

}
