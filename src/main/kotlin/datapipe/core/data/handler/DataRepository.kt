package datapipe.core.data.handler

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.generator.metadata
import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.MetadataList
import java.io.BufferedWriter
import java.io.FileWriter
import java.lang.reflect.Field

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

    fun saveToCSV(path: String) {
        BufferedWriter(FileWriter(path)).use {
            val fields = getIncludedFields()
            it.write(fields.joinToString(";", transform = Field::getName))
            it.newLine()

            for (value in values) {
                it.write(fields.joinToString(";", transform = { "\"${it.get(value)}\"" }))
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

    fun getIncludedFields(excluded: List<String> = emptyList()): List<Field> {
        val paths = metadataPropertiesPaths(containsClass.metadata())
        println(paths)
        return containsClass.fields.toList()
    }

    private fun merge(prefix: String, name: String) = if (prefix == "") name else "$prefix.$name"

    private fun metadataPropertiesPaths(metadataClass: MetadataClass, prefix: String = ""): List<String> {
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

}
