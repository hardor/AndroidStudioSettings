package ru.profapp.ranobe.Adapters

import ru.profapp.ranobe.Models.Chapter

data class ParentDataItem(val parentName: String = "", val childDataItems: List<Chapter> = listOf(), var canRead: Boolean = true)