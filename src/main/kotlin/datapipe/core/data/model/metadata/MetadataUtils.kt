package datapipe.core.data.model.metadata

import com.google.gson.JsonElement

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
        val properties = mutableSetOf<MetadataProperty>()
        for ((key, value) in jsonElement.asJsonObject.entrySet()) {
            properties.add(MetadataProperty(key, constructMetadataFromJson(value)))
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

private fun merge(prefix: String, name: String) = if (prefix == "") name else "$prefix.$name"

fun metadataPropertiesPaths(metadataClass: MetadataClass, prefix: String = ""): List<String> {
    val result = mutableListOf<String>()
    for (child in metadataClass.properties) {
        val childPaths = when {
            child.type is MetadataClass -> metadataPropertiesPaths(child.type, merge(prefix, child.name))
            child.type is MetadataList && child.type.containsType is MetadataClass ->
                metadataPropertiesPaths(child.type.containsType, merge(prefix, child.name))
            else -> listOf(merge(prefix, child.name))
        }
        result.addAll(childPaths)
    }
    return result
}