package ru.profapp.RanobeReader.CustomElements

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Single
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Network.Repositories.RulateRepository
import ru.profapp.RanobeReader.R

class RulateLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_rulate)
        sharedPref = context.getSharedPreferences(Constants.Rulate_Login_Pref, Context.MODE_PRIVATE)
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