package ru.profapp.RanobeReader.Adapters

import ru.profapp.RanobeReader.Models.Chapter

data class ParentDataItem(val parentName: String = "", val childDataItems: List<Chapter> = listOf(), var canRead: Boolean = true)