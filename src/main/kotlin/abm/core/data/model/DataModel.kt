package abm.core.data.model

import abm.core.data.model.metadata.*
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.Reader
import java.io.StringReader

/**
 * @author: andrei shlykov
 * @since: 19.01.2018
 */
@Deprecated("use pipe elements")
class DataModel {

    companion object {
        val parser = JsonParser()
    }

    var metadata: Metadata = PrimitiveNull

    fun generalize(json: String): Metadata = generalize(StringReader(json))

    fun generalize(reader: Reader): Metadata {
        val jsonElement = parser.parse(reader)
        val newMetadata = constructMetadataFromJson(jsonElement)
        val oldMetadata = metadata
        metadata = newMetadata combine oldMetadata
        return oldMetadata
    }

//    fun getMetadataByPath(path: String): Metadata {
//        val propertyNames = path.split(".")
//        var current = metadata
//
//        for (name in propertyNames) {
//            if (current is MetadataClass) {
//                current = current.properties[name] ?: throw RuntimeException()
//            } else {
//                throw RuntimeException()
//            }
//        }
//        return current
//    }

//    fun removeProperty(path: String) {
//        val lastSeparator = path.lastIndexOf(".")
//        val propertyName = path.substring(lastSeparator+1)
//        val parent = if (lastSeparator > -1) {
//            val parentPath = path.substring(0, lastSeparator)
//            getMetadataByPath(parentPath)
//        } else {
//            metadata
//        }
//
//        if (parent is MetadataClass) {
//            parent.properties.remove(propertyName)
//        } else {
//            throw RuntimeException("don't have property: $propertyName")
//        }
//
//    }

    override fun toString() = metadata.toString()

    private fun constructMetadataFromJson(jsonElement: JsonElement): Metadata = when {
        jsonElement.isJsonObject -> {
            val properties = mutableSetOf<PropertyMetadata>()
            for ((key, value) in jsonElement.asJsonObject.entrySet()) {
                properties.add(PropertyMetadata(key, constructMetadataFromJson(value)))
            }
            MetadataClass(properties)
        }
        jsonElement.isJsonArray -> {
            var containedType: Metadata = PrimitiveNull
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

}