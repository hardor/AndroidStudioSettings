package ru.profapp.RanobeReader.Helpers

import android.util.Log
import com.crashlytics.android.Crashlytics
import ru.profapp.RanobeReader.BuildConfig

class LogHelper {

    companion object {

        private const val showLog = BuildConfig.USE_LOG
        @JvmStatic
        fun SendMessage(type: LogType, Tag: String, Message: String) {
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
        }

        @JvmStatic
        fun SendError(type: LogType, Tag: String, Message: String = "", exception: Throwable) {
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
            Crashlytics.logException(exception)
            if (Message.isNotEmpty()) {
                Crashlytics.log(Message)
            }
        }

    }

    enum class LogType {
        ASSERT, DEBUG, ERROR, INFO, VERBOSE, WARN
    }
}
