package ru.profapp.RanobeReader.CustomElements

import android.content.Context
import android.util.AttributeSet
import io.reactivex.Single
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.Network.Repositories.RanobeRfRepository
import ru.profapp.RanobeReader.R

class RanoberfLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_action_ranoberf)
        sharedPref = context.getSharedPreferences(Constants.Ranoberf_Login_Pref, Context.MODE_PRIVATE)
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