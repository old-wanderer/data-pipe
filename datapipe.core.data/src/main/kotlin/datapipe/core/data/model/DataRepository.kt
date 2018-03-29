package datapipe.core.data.model

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.generator.metadata
import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.metadataPropertiesPaths
import datapipe.core.data.model.metadata.transformer.MetadataTransformer
import datapipe.core.data.model.metadata.transformer.MetadataTransformerBuilder
import datapipe.core.data.model.metadata.transformer.transformTo
import java.io.BufferedWriter
import java.io.FileWriter

/**
 * @author: andrei shlykov
 * @since: 27.01.2018
 */
class DataRepository(val containsClass: Class<GeneratedClass>,
                     private val values: MutableList<GeneratedClass>): Iterable<GeneratedClass> {

    val size get() = this.values.size

    operator fun get(range: IntRange) =
            DataRepository(containsClass, values.subList(range.first, range.last))

    operator fun get(index: Int) = values[index]

    fun transform(builder: MetadataTransformerBuilder.() -> Unit): DataRepository {
        val transformer = containsClass.metadata().transformTo(builder)
        return DataRepository(
                transformer.destinationMetadata.generatedClass,
                values.map(transformer::transform).toMutableList())

    }

    // TODO test
    fun saveToCSV(path: String) {
        BufferedWriter(FileWriter(path)).use {
            val paths = getIncludedFields()
            it.write(paths.joinToString(";"))
            it.newLine()

            for (value in values) {
                it.write(paths.joinToString(";", transform = { "\"${value.getPropertyValue(it)}\"" }))
                it.newLine()
            }
        }
    }

    fun calcUniqueValue(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        for (field in containsClass.fields) {
            val getter = containsClass.getField(field.name)::get
            val distinct = values.map(getter).distinct().size
            result[field.name] = distinct
        }
        return result
    }

    fun getUniqueValues(path: String): List<Any> {
        val getter = containsClass.getField(path)::get
        return values.map(getter).distinct()
    }

    override fun iterator() = values.iterator()

    private fun getIncludedFields(excluded: List<String> = emptyList()): List<String> =
            metadataPropertiesPaths(containsClass.metadata()) - excluded

}
