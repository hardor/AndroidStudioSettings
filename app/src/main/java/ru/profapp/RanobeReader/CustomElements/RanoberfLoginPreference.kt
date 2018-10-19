package ru.profapp.RanobeReader.CustomElements

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.profapp.RanobeReader.Common.Constants
import ru.profapp.RanobeReader.JsonApi.RanobeRfRepository
import ru.profapp.RanobeReader.JsonApi.RulateRepository
import ru.profapp.RanobeReader.R

class RanoberfLoginPreference(context: Context, attrs: AttributeSet) : BaseLoginPreference(context, attrs) {


    init {
        setDialogIcon(R.mipmap.ic_action_ranoberf)
    }

    override fun auth(): Single<Array<String>> {

        val res = super.auth()
        return res.flatMap { it ->

            if ( it[0].toBoolean()) {
                return@flatMap RanobeRfRepository.login(username, password)
            }
            return@flatMap Single.just(it)
        }
    }

}