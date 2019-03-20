package ru.profapp.ranobe.helpers

import android.util.Log
import com.crashlytics.android.Crashlytics
import ru.profapp.ranobe.BuildConfig

private const val showLog = BuildConfig.USE_LOG

fun logMessage(type: LogType, Tag: String, Message: String, sendMessage: Boolean = true) {
    if (showLog) {
        when (type) {
            LogType.INFO -> Log.i(Tag, Message)
            LogType.WARN -> Log.w(Tag, Message)
            LogType.ASSERT -> Log.wtf(Tag, Message)
            LogType.DEBUG -> Log.d(Tag, Message)
            LogType.ERROR -> Log.e(Tag, Message)
            LogType.VERBOSE -> Log.v(Tag, Message)
        }

    }

    if (sendMessage && Message.isNotEmpty()) {
        Crashlytics.log(Message)
    }
}

fun logError(Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
    logCrashlystics(LogType.ERROR, Tag, Message, exception, sendError)
}

fun logInfo(Tag: String, Message: String = "") {
    logCrashlystics(LogType.INFO, Tag, Message)
}

fun logWarn(Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
    logCrashlystics(LogType.WARN, Tag, Message, exception, sendError)
}

fun logAssert(Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
    logCrashlystics(LogType.ASSERT, Tag, Message, exception, sendError)
}

fun logDebug(Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
    logCrashlystics(LogType.DEBUG, Tag, Message, exception, sendError)
}

fun logVerbose(Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
    logCrashlystics(LogType.VERBOSE, Tag, Message, exception, sendError)
}

fun logCrashlystics(type: LogType,
                    Tag: String,
                    Message: String = "",
                    exception: Throwable? = null,
                    sendError: Boolean = true) {
    if (showLog) {
        when (type) {
            LogType.INFO -> Log.i(Tag, Message, exception)
            LogType.WARN -> Log.w(Tag, Message, exception)
            LogType.ASSERT -> Log.wtf(Tag, Message, exception)
            LogType.DEBUG -> Log.d(Tag, Message, exception)
            LogType.ERROR -> Log.e(Tag, Message, exception)
            LogType.VERBOSE -> Log.v(Tag, Message, exception)
        }

    }
    if (sendError && exception != null) {
        Crashlytics.logException(exception)
        if (Message.isNotEmpty()) {
            Crashlytics.log(Message)
        }
    }
}

enum class LogType {
    ASSERT, DEBUG, ERROR, INFO, VERBOSE, WARN
}

