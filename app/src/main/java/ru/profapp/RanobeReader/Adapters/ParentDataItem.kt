package ru.profapp.RanobeReader.Adapters

import ru.profapp.RanobeReader.Models.Chapter

internal class ParentDataItem(internal val parentName: String = "", internal val childDataItems: List<Chapter>)