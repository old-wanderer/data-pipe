package abm.core.data.model.metadata

import com.google.gson.JsonElement

/**
 * @author: Andrei Shlykov
 * @since: 27.02.2018
 */
// todo перенести сюда все утильные методы

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
    jsonElement.isJsonNull -> PrimitiveNull
    else -> PrimitiveNull
}