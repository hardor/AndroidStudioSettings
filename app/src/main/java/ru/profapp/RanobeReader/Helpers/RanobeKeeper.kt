package ru.profapp.RanobeReader.Helpers

import ru.profapp.RanobeReader.Common.Constants

/**
 * Created by Ruslan on 03.03.2018.
 */

class RanobeKeeper {


    companion object {

        var ranobeRfToken: String? = null

        var chapterTextSize: Int? = null

        var autoSaveText: Boolean = false

        var fragmentType: Constants.FragmentType? = null

    }
}
