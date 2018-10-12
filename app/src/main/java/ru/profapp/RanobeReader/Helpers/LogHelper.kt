package ru.profapp.RanobeReader.Helpers

import android.util.Log
import com.crashlytics.android.Crashlytics
import ru.profapp.RanobeReader.BuildConfig

class LogHelper {

    companion object {

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


        fun logError(type: LogType, Tag: String, Message: String = "", exception: Throwable, sendError: Boolean = true) {
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
            if (sendError) {
                Crashlytics.logException(exception)
                if (Message.isNotEmpty()) {
                    Crashlytics.log(Message)
                }
            }
        }

    }

    enum class LogType {
        ASSERT, DEBUG, ERROR, INFO, VERBOSE, WARN
    }
}
