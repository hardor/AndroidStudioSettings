package ru.profapp.RanobeReader.CustomElements

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.JsonApi.RulateRepository
import ru.profapp.RanobeReader.R

class RulateLoginPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs), DialogInterface.OnClickListener {

    private lateinit var sharedPref: SharedPreferences
    // Current value
    private val mCurrentValue: String? = null
    // View elements
    private lateinit var mLoginEditor: EditText
    private lateinit var mPasswordEditor: EditText

    init {
        positiveButtonText = "Login"
        negativeButtonText = "Cancel"
        setDialogIcon(R.mipmap.ic_rulate)
    }

    override fun onCreateDialogView(): View {

        // Inflate layout
        val view = View.inflate(context, R.layout.activity_login, null)

        val context = this.context
        sharedPref = context.getSharedPreferences(Constants.Rulate_Login_Pref, Context.MODE_PRIVATE)

        val value = sharedPref.getString(Constants.KEY_Login, "")
        // Setup SeekBar
        mLoginEditor = view.findViewById(R.id.login_login)
        mLoginEditor.setText(value)
        mPasswordEditor = view.findViewById(R.id.login_password)

        return view
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        // Return if change was cancelled
        if (!positiveResult) {
            return
        }

        // Persist current value if needed
        if (shouldPersist()) {
            persistString(mCurrentValue)
        }

        // Notify activity about changes (to update preference summary line)
        notifyChanged()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            auth()
        }
    }

    private fun auth() {

        val username = mLoginEditor.text.toString()
        val password = mPasswordEditor.text.toString()

        sharedPref.edit().putString(Constants.KEY_Login, username).apply()

        val alert = AlertDialog.Builder(context).create()
        // Check if username, password is filled
        if (username.isNotBlank() && password.isNotBlank()) {
            RulateRepository.login(username, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        val resBool = java.lang.Boolean.valueOf(result[0])
                        if (resBool) {
                            sharedPref.edit().putString(Constants.KEY_Token, result[2]).apply()
                            summary = username
                        } else {
                            sharedPref.edit().putString(Constants.KEY_Token, "").apply()

                            summary = context.getString(R.string.summary_login)
                        }

                        alert.setTitle(username)
                        alert.setMessage(result[1])
                        alert.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialog, which -> }
                        alert.show()
                    }, {
                        sharedPref.edit().putString(Constants.KEY_Token, "").apply()

                        summary = context.getString(R.string.summary_login)

                        alert.setTitle(username)
                        alert.setMessage(context.getString(R.string.responce_error))
                        alert.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialog, which -> }
                        alert.show()

                    })

        } else {
            // user didn't entered username or password
            // Show alert asking him to enter the details

            alert.setTitle(context.getString(R.string.login_failed))
            alert.setMessage(context.getString(R.string.enter_user_pass))
            alert.setButton(Dialog.BUTTON_POSITIVE, "OK") { dialog, which ->
                sharedPref.edit().putString(Constants.KEY_Token, "").apply()
                summary = context.getString(R.string.summary_login)
            }
            alert.setButton(Dialog.BUTTON_NEUTRAL, "Cancel") { dialog, which ->

            }
            alert.show()
        }

    }

}