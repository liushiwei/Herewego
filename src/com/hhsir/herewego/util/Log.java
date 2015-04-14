
package com.hhsir.herewego.util;

public class Log {

    public static int e(Class c, String message) {
        if (GlobalConfig.DEBUD)
            return println(android.util.Log.ERROR, c.getSimpleName(), message);
        else
            return 0;
    }

    public static int d(Class c, String message) {
        if (GlobalConfig.DEBUD)
            return println(android.util.Log.DEBUG, c.getSimpleName(), message);
        else
            return 0;
    }

    public static int i(Class c, String message) {
        if (GlobalConfig.DEBUD)
            return println(android.util.Log.INFO, c.getSimpleName(), message);
        else
            return 0;
    }

    public static int w(Class c, String message) {
        if (GlobalConfig.DEBUD)
            return println(android.util.Log.WARN, c.getSimpleName(), message);
        else
            return 0;
    }

    public static int v(Class c, String message) {
        if (GlobalConfig.DEBUD)
            return println(android.util.Log.VERBOSE, c.getSimpleName(), message);
        else
            return 0;
    }

    public static int println(int priority, Class tag, String msg) {
        return android.util.Log.println(priority, tag.getSimpleName(), msg);
    }

    public static int println(int priority, String tag, String msg) {
        if (priority > GlobalConfig.LEVEL)
            return android.util.Log.println(priority, tag, msg);
        else
            return 0;
    }

}
