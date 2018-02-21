package abm.core.data.pipeline

import abm.core.data.generator.ClassGenerator
import abm.core.data.handler.DataRepository
import abm.core.data.model.metadata.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.FileReader
import java.util.*
import java.util.stream.Collectors

/**
 * @author: Andrei Shlykov
 * @since: 03.02.2018
 */
object Pipelines {

    fun extractModelFrom(path: String, limit: Long = -1): PipelineElement<Unit, Metadata> =
            ModelExtractor(path, limit)

    fun generateClass(): PipelineElement<Metadata, Class<*>> =
            PipelineElement { ClassGenerator.generateClass(it as MetadataClass) }

    fun parseData(path: String, limit: Long = -1): PipelineElement<Class<*>, DataRepository> =
            DataParser(path, limit)

    // TODO возможность задать isBadName, correctBadName и обобщить механиз реконструкции метадаты
    fun aliasForBadNames(): PipelineElement<Metadata, Metadata> =
            PipelineElement { source ->

                fun String.isBadName() = this[0] == '/'
                fun String.correctBadName() = this.substring(1)
                fun MetadataClass.processProperties(): Set<PropertyMetadata> {
                    fun Metadata.processIfMetadataClass(): Metadata =  if (this is MetadataClass)
                        MetadataClass(this.processProperties()) else this
                    return this.properties.map {
                        if (it.name.isBadName()) {
                            PropertyMetadata(it.name.correctBadName(),
                                    it.type.processIfMetadataClass(), it.aliasNames + it.name)
                        } else {
                            PropertyMetadata(it.name, it.type.processIfMetadataClass())
                        }
                    }.toSet()
                }

                if (source is MetadataClass) {
                    MetadataClass(source.processProperties())
                } else {
                    source!!
                }
            }


    fun <T> process(task: (T?) -> Unit): PipelineElement<T, T> =
        PipelineElement({
            task(it)
            it!!
        })


    private class DataParser(val path: String, val limit: Long)
        :PipelineElement<Class<*>, DataRepository>
    ({ clazz ->
        val reader = BufferedReader(FileReader(path))
        val gson = Gson()

        var stream = reader.lines()
        if (limit > 0) {
            stream = stream.limit(limit)
        }
        val lst = stream.map { str -> gson.fromJson(str, clazz) }.collect(Collectors.toList())

        DataRepository(clazz!!, lst)
    })

    private class ModelExtractor(val path: String, val limit: Long)
        : PipelineElement<Unit, Metadata>
    ({
        val reader = BufferedReader(FileReader(path))
        val parser = JsonParser()

        var stream = reader.lines()
        if (limit > 0) {
            stream = stream.limit(limit)
        }
        stream
                .map { constructMetadataFromJson(parser.parse(it)) }
                .reduce(PrimitiveNull, Metadata::combine)
    })

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