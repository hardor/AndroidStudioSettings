package ru.profapp.ranobe.models

import ru.profapp.ranobe.common.Constants
import ru.profapp.ranobe.network.dto.ranobeHubDTO.RanobeHubComment
import ru.profapp.ranobe.network.dto.rulateDTO.RulateComment

data class Comment(
        val image: String? = null,
        val comment: String? = null,
        val createdAt: Long? = null,
        val name: String? = null
) {


    constructor(r: RulateComment) : this(r.avatar, r.body, r.time, r.author)
    constructor(r: RanobeHubComment) : this( Constants.RanobeSite.RanobeHub.url + r.commenter.avatar.thumb, r.comment,r.createdAt,r.commenter.name)
}




