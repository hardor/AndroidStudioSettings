package ru.profapp.RanobeReader.JsonApi.CustomDeserializer

import com.google.gson.*
import ru.profapp.RanobeReader.JsonApi.RulateDTO.RulateBook
import java.lang.reflect.Type

internal class RulateBookDeserializer : JsonDeserializer<RulateBook> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RulateBook {

        val obj = (json as JsonObject).get("comments")
        if (obj != null && !obj.isJsonArray) {
            json.remove("comments")
        }
        return Gson().fromJson(json, RulateBook::class.java)

    }

}