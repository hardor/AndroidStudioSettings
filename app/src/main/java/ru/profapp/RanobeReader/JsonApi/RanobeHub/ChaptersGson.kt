package ru.profapp.RanobeReader.JsonApi.RanobeHub

data class ChaptersGson(
        val data: List<Volumes>
)

data class Volumes(
        val id: Int,
        val idRanobe: Int,
        val num: String,
        val name: String,
        val idStatus: Int,
        val statusOriginal: Any? = null,
        val idOriginal: Any? = null,
        val idRecheckStatus: Any? = null,
        val imageStorage: String,
        val createdAt: String,
        val updatedAt: String,
        val statusName: Status,
        val chapters: List<tChapter> = ArrayList()
)

data class tChapter(
        val id: Int,
        val idRanobe: Int,
        val num: String,
        val numVolume: Int,
        val idOriginal: Any? = null,
        val name: String,
        val createdAt: String
)

data class Status(
        val name: String
)
