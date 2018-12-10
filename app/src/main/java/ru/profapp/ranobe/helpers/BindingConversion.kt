package ru.profapp.ranobe.helpers

import androidx.databinding.BindingConversion
import ru.profapp.ranobe.network.dto.rulateDTO.RulateComment

@BindingConversion
fun convertHobbiesToString(comments: List<RulateComment>): String {
    val sb = StringBuilder()
    for (comment in comments) {
        if (sb.length > 0) sb.append(", ")
        sb.append(comment.body)
    }

    return sb.toString()
}