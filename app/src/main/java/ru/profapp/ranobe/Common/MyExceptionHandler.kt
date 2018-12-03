package ru.profapp.ranobe.Common

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ru.profapp.ranobe.Activities.MainActivity
import ru.profapp.ranobe.Helpers.LogType
import ru.profapp.ranobe.Helpers.logError

import ru.profapp.ranobe.MyApp

class MyExceptionHandler(private val activity: Activity) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        var message = "Uncaught exception"
        if(MyApp.ranobe?.url !=null)
            message = MyApp.ranobe!!.url

        logError(LogType.ERROR, activity.packageName, message, ex)
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("crash", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val mgr = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)

        activity.finish()
        System.exit(2)
    }
}