package ru.profapp.ranobe.adapters

import ru.profapp.ranobe.models.Chapter

data class ParentDataItem(val parentName: String = "",
                          val childDataItems: List<Chapter> = listOf(),
                          var canRead: Boolean = true)