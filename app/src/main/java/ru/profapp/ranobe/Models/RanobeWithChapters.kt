package ru.profapp.ranobe.Models

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RanobeWithChapters

        if (ranobe != other.ranobe) return false
        if (chapterList != other.chapterList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ranobe.hashCode()
        result = 31 * result + chapterList.hashCode()
        return result
    }

}
