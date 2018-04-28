package ru.profapp.RanobeReader.Helpers;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import ru.profapp.RanobeReader.BuildConfig;
import ru.profapp.RanobeReader.Common.StringResources;

public class MyLog {

    private static volatile MyLog instance;
    private static final boolean showLog = BuildConfig.USE_LOG;

    private MyLog() {
    }

    public static MyLog getInstance() {
        if (instance == null) {
            synchronized (MyLog.class) {
                if (instance == null) {
                    instance = new MyLog();
                }
            }
        }
        return instance;
    }

    public static void SendMessage(StringResources.LogType type, String Tag, String Message) {
        if (showLog) {
            switch (type) {
                case INFO:
                    Log.i(Tag, Message);
                    break;
                case WARN:
                    Log.w(Tag, Message);
                    break;
                case ASSERT:
                    Log.wtf(Tag, Message);
                    break;
                case DEBUG:
                    Log.d(Tag, Message);
                    break;
                case ERROR:
                    Log.e(Tag, Message);
                    break;
                case VERBOSE:
                    Log.v(Tag, Message);
                    break;

            }

        }
    }

    public static void SendError(StringResources.LogType type, String Tag, String Message,
            Throwable exception) {
        if (showLog) {
            switch (type) {
                case INFO:
                    Log.i(Tag, Message, exception);
                    break;
                case WARN:
                    Log.w(Tag, Message, exception);
                    break;
                case ASSERT:
                    Log.wtf(Tag, Message, exception);
                    break;
                case DEBUG:
                    Log.d(Tag, Message, exception);
                    break;
                case ERROR:
                    Log.e(Tag, Message, exception);
                    break;
                case VERBOSE:
                    Log.v(Tag, Message, exception);
                    break;

            }

        }
        Crashlytics.logException(exception);
    }
}
