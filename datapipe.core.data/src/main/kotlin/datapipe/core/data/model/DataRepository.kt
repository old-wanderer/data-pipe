package datapipe.core.data.model

import java.io.BufferedWriter
import java.io.FileWriter
import java.lang.reflect.Field

/**
 * @author: andrei shlykov
 * @since: 27.01.2018
 */
class DataRepository(val containsClass: Class<*>,
                     private val values: MutableList<Any>): Iterable<Any> {

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

    private fun getIncludedFields(excluded: List<String> = emptyList()): List<Field> =
            containsClass.fields.toList()

}
