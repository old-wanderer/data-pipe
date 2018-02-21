package abm.core.data.handler

import java.io.BufferedWriter
import java.io.FileWriter
import java.lang.reflect.Field
import java.util.stream.Collectors

/**
 * @author: andrei shlykov
 * @since: 27.01.2018
 */
class DataRepository(val containsClass: Class<*>,
                     private val values: MutableList<Any>): Iterable<Any> {

    companion object {

        fun analyzePropertyRelationship(a: DataRepository, b: DataRepository,
                                        af: String, bf: String): Triple<Int, Int, Int> {

            val getterA = a.containsClass.getField(af)::get
            val getterB = b.containsClass.getField(bf)::get

            val mupb: Map<Any, List<Any>> = b.values.groupBy(getterB, { it })
            val mupres = HashMap<Any, Int>(a.size)

            a.values
                    .filter { getterA(it) in mupb }
                    .forEach { mupres.compute(it) { _, v ->
                        if (v == null) mupb[getterA(it)]!!.size else v + mupb[getterA(it)]!!.size } }

            val min = if (mupres.size == b.size) mupres.values.min()!! else 0
            val max = mupres.values.max() ?: 0
            val total = mupres.values.sum()

            return Triple(min, max, total)
        }
    }
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
