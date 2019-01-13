package ru.profapp.ranobe.pref

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlin.reflect.KProperty

class PreferenceDelegates<T>(val context: Context, val prefRes: String, private val key: String, val defaultValue: T) {

    val prefs: SharedPreferences by lazy {
        if (prefRes.isEmpty())
            PreferenceManager.getDefaultSharedPreferences(context)
        else
            context.getSharedPreferences(prefRes, Context.MODE_PRIVATE)
    }

    constructor (context: Context, prefResId: Int? = null, keyResId: Int, defaultValue: T) : this(context, if (prefResId == null) "" else context.getString(prefResId), context.getString(keyResId), defaultValue)

    constructor (context: Context, prefResId: Int? = null, keyRes: String, defaultValue: T) : this(context, if (prefResId == null) "" else context.getString(prefResId), keyRes, defaultValue)

    constructor (context: Context, prefRes: String?, keyResId: Int, defaultValue: T) : this(context, prefRes
            ?: "", context.getString(keyResId), defaultValue)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreferences(key, defaultValue)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        savePreference(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun findPreferences(key: String, defaultValue: T): T {
        with(prefs)
        {
            val result: Any = when (defaultValue) {
                is Boolean -> getBoolean(key, defaultValue)
                is Int -> getInt(key, defaultValue)
                is Long -> getLong(key, defaultValue)
                is Float -> getFloat(key, defaultValue)
                is String -> getString(key, defaultValue)
                else -> throw IllegalArgumentException()
            }
            return result as T
        }
    }

    private fun savePreference(key: String, value: T) {
        with(prefs.edit())
        {
            when (value) {
                null -> remove(key)
                is Boolean -> putBoolean(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is String -> putString(key, value)
                else -> throw IllegalArgumentException()
            }.apply()
        }
    }
}