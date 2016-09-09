package com.github.dfa.diaspora_android.util;

import com.github.dfa.diaspora_android.App;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Class that saves logs eg. for later debugging.
 * TODO: Differentiate log types (error/debug/info...)
 * Created by vanitas on 09.09.16.
 */
public class Log extends Observable{
    public static Log instance;
    private SimpleDateFormat dateFormat;
    private ArrayList<String> logBuffer;
    private ArrayList<Observer> observers;

    private Log() {
        logBuffer = new ArrayList<>();
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        observers = new ArrayList<>();
    }

    public static Log getInstance() {
        if(instance == null) instance = new Log();
        return instance;
    }

    private static String time() {
        return getInstance().dateFormat.format(new Date())+": ";
    }

    public static void d(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.d(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static void e(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.e(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static void i(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.i(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static void v(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.v(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static void w(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.w(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static void wtf(String tag, String msg) {
        Log l = getInstance();
        android.util.Log.wtf(tag, msg);
        l.logBuffer.add(time()+msg);
        l.notifyLogBufferChanged();
    }

    public static ArrayList<String> getLogBufferArray() {
        return getInstance().logBuffer;
    }

    public static String getLogBuffer() {
        String out = "";
        for(String s : getInstance().logBuffer) {
            out = out + s + "\n";
        }
        return out;
    }

    private void notifyLogBufferChanged() {
        if(observers == null) return;
        for(Observer o : observers) {
            if(o != null) {
                o.update(this, null);
            }
        }
    }

    public static void addLogObserver(Observer observer) {
        getInstance().observers.add(observer);
    }

    public static void removeLogObserver(Observer o) {
        getInstance().observers.remove(o);
    }
}
