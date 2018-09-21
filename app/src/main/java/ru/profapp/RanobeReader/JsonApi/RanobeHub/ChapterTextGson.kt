package ru.profapp.RanobeReader.JsonApi.RanobeHub


data class ChapterTextGson(
        val ranobe: tRanobe,
        val volume: Volume,
        val chapter: Chapter,
        val manage: Manage,
        val bookmark: Boolean
)

data class Chapter(
        val id: Long,
        val num: Long,
        val name: String,
        val content: String
)

data class Manage(
        val chapterNext: ChapterNextClass,
        val chapterPrev: ChapterNextClass
)

data class ChapterNextClass(
        val ranobeID: Long,
        val chapterNum: Long,
        val volumeNum: Long,
        val chapterName: String
)

data class tRanobe(
        val id: Long,
        val name: String
)

data class Volume(
        val num: Long
)

