package ru.profapp.RanobeReader.CustomElements

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.R

open class BaseLoginPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs), DialogInterface.OnClickListener {

    private val mCurrentValue: String? = null
    lateinit var sharedPref: SharedPreferences
    // Current value
    //    val mCurrentValue: String? = null
    // View elements
    lateinit var loginEditor: EditText
    lateinit var resultTextView: TextView
    lateinit var passwordEditor: TextInputEditText
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var username: String = ""
    var password: String = ""

    init {
        positiveButtonText = "Login"
        negativeButtonText = "Cancel"
    }

    override fun onCreateDialogView(): View {

        // Inflate layout
        val view = View.inflate(context, R.layout.diaolog_login, null)

        val value = sharedPref.getString(Constants.KEY_Login, "")

        resultTextView = view.findViewById(R.id.tV_login_resultLabel)
        loginEditor = view.findViewById(R.id.eT_login_email)
        loginEditor.setText(value)
        passwordEditor = view.findViewById(R.id.eT_login_password)

        return view
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        if (!positiveResult) {
            return
        }
        if (shouldPersist()) {
            persistString(mCurrentValue)
        }
        notifyChanged()
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
        val alert = (dialog as AlertDialog)
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

            resultTextView.visibility = View.GONE
            val loginRequest = auth()
                    .map { result ->

                        val resBool = java.lang.Boolean.valueOf(result[0])
                        if (resBool) {
                            return@map Pair(true, result[2])
                        } else {
                            return@map Pair(false, result[1])
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->

                        if (result.first) {
                            sharedPref.edit().putString(Constants.KEY_Token, result.second).apply()
                            summary = username
                            alert.dismiss()
                        } else {
                            sharedPref.edit().putString(Constants.KEY_Token, "").apply()
                            resultTextView.text = result.second
                            resultTextView.visibility = View.VISIBLE
                        }

                    }, { error ->
                        sharedPref.edit().putString(Constants.KEY_Token, "").apply()
                        resultTextView.text = context.getString(R.string.response_error)
                        resultTextView.visibility = View.VISIBLE
                    })
            compositeDisposable.add(loginRequest)
        }
        alert.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setOnClickListener {
                    alert.dismiss()
                }
    }

    open fun auth(): Single<Array<String>> {

        username = loginEditor.text.toString()

        password = passwordEditor.text.toString()

        sharedPref.edit().putString(Constants.KEY_Login, username).apply()


        if (!username.isNotBlank() || !password.isNotBlank()) {
            return Single.just(arrayOf("false", context.getString(R.string.enter_user_pass)))
        }

        return Single.just(arrayOf("true", ""))
    }

    override fun onActivityDestroy() {
        super.onActivityDestroy()
        compositeDisposable.clear()
    }
}