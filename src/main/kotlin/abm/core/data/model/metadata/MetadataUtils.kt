package abm.core.data.model.metadata

import abm.core.data.model.metadata.parser.*
import com.google.gson.JsonElement
import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 27.02.2018
 */
// todo перенести сюда все утильные методы

/**
 * строит метаданные для переданного json объекта
 *
 * @param jsonElement
 * @return метаданные соответствующие jsonElement
 */
fun constructMetadataFromJson(jsonElement: JsonElement): MetadataType = when {
    jsonElement.isJsonObject -> {
        val properties = mutableSetOf<PropertyMetadata>()
        for ((key, value) in jsonElement.asJsonObject.entrySet()) {
            properties.add(PropertyMetadata(key, constructMetadataFromJson(value)))
        }
        MetadataClass(properties)
    }
    jsonElement.isJsonArray -> {
        var containedType: MetadataType = PrimitiveNull
        for (value in jsonElement.asJsonArray) {
            containedType = containedType combine constructMetadataFromJson(value)
        }
        MetadataList(containedType)
    }
    jsonElement.isJsonPrimitive -> {
        val jsonPrimitive = jsonElement.asJsonPrimitive
        when {
            jsonPrimitive.isNumber -> PrimitiveDouble
            jsonPrimitive.isBoolean -> PrimitiveBoolean
            else -> PrimitiveString
        }
    }
    else -> PrimitiveNull
}