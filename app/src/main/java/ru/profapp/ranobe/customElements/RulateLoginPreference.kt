package ru.profapp.ranobe.customElements

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Single
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.network.repositories.RulateRepository

class RulateLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_rulate)
        ranobeSite = Constants.RanobeSite.Rulate
    }

    override var sharedToken
        get() = MyApp.preferencesManager.rulateToken
        set(value) {
            MyApp.preferencesManager.rulateToken = value
        }

    override var sharedLogin
        get() = MyApp.preferencesManager.rulateLogin
        set(value) {
            MyApp.preferencesManager.rulateLogin = value
        }

    override fun auth(): Single<Array<String>> {

        val res = super.auth()
        return res.flatMap { it ->

            if (it[0].toBoolean()) {
                return@flatMap RulateRepository.login(username, password)
            }

            return@flatMap Single.just(it)
        }
    }
}