package ru.profapp.RanobeReader.CustomElements

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.DialogPreference
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReader.R

class RanoberfLoginPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs), DialogInterface.OnClickListener {

    private lateinit var sharedPref: SharedPreferences
    // Current value
    private val mCurrentValue: String? = null
    // View elements
    private var mLoginEditor: EditText? = null
    private var mPasswordEditor: EditText? = null

    init {
        positiveButtonText = "Login"
        negativeButtonText = "Cancel"
        setDialogIcon(R.mipmap.ic_action_ranoberf)
    }

    override fun onCreateDialogView(): View {

        val view = View.inflate(context, R.layout.activity_login, null)

        val context = this.context
        sharedPref = context.getSharedPreferences(Constants.Ranoberf_Login_Pref, Context.MODE_PRIVATE)

        val value = sharedPref.getString(Constants.KEY_Login, "")

        // Setup SeekBar

        mLoginEditor = view.findViewById(R.id.login_login)
        mLoginEditor!!.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        mLoginEditor!!.hint = getContext().resources.getString(R.string.email)
        mLoginEditor!!.setText(value)
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

        val username = mLoginEditor!!.text.toString()
        val password = mPasswordEditor!!.text.toString()

        sharedPref.edit().putString(Constants.KEY_Login, username).apply()

        val alert = AlertDialog.Builder(context).create()
        // Check if username, password is filled
        if (username.isNotBlank() && password.isNotBlank()) {
            RanobeRfRepository.login(username, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        val resBool = java.lang.Boolean.valueOf(result[0])
                        if (resBool) {
                            sharedPref.edit().putString(Constants.KEY_Token, result[2]).apply()
                            summary = username
                            alert.setMessage(context.getString(R.string.auth_succes))
                        } else {
                            sharedPref.edit().putString(Constants.KEY_Token, "").apply()
                            summary = context.getString(R.string.summary_login)
                            alert.setMessage(context.getString(R.string.auth_error))
                        }

                        alert.setTitle(username)
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