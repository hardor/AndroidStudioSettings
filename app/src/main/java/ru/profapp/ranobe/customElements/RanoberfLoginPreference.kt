package ru.profapp.ranobe.customElements

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Single
import ru.profapp.ranobe.MyApp
import ru.profapp.ranobe.R
import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.network.repositories.RanobeRfRepository

class RanoberfLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_ranoberf)
        ranobeSite = Constants.RanobeSite.RanobeRf
    }

    override var sharedToken
        get() = MyApp.preferencesManager.ranoberfToken
        set(value) {
            MyApp.preferencesManager.ranoberfToken = value
        }

    override var sharedLogin
        get() = MyApp.preferencesManager.ranoberfLogin
        set(value) {
            MyApp.preferencesManager.ranoberfLogin = value
        }
    override fun auth(): Single<Array<String>> {

        val res = super.auth()
        return res.flatMap { it ->

            if (it[0].toBoolean()) {
                return@flatMap RanobeRfRepository.login(username, password)
            }
            return@flatMap Single.just(it)
        }
    }

}