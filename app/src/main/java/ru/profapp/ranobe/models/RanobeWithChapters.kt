package ru.profapp.ranobe.models

import androidx.room.Embedded
import androidx.room.Relation
import java.util.*

/**
 * Created by Ruslan on 09.02.2018.
 */

class RanobeWithChapters {
    @Embedded
    lateinit var ranobe: Ranobe

    @Relation(parentColumn = "Url", entityColumn = "RanobeUrl", entity = Chapter::class)
    var chapterList: MutableList<Chapter> = ArrayList()


}
