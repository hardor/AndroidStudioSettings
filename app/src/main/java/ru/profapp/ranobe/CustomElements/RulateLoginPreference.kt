package ru.profapp.ranobe.CustomElements

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Single
import ru.profapp.ranobe.Common.Constants
import ru.profapp.ranobe.Network.Repositories.RulateRepository
import ru.profapp.ranobe.R

class RulateLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_rulate)
        sharedPref = context.applicationContext.getSharedPreferences(Constants.Rulate_Login_Pref, Context.MODE_PRIVATE)
        ranobeSite = Constants.RanobeSite.Rulate
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