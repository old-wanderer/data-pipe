package abm.core.data.handler

import abm.core.data.model.DataModel
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStream
import java.util.stream.Collectors

/**
 * @author: andrei shlykov
 * @since: 19.01.2018
 */
@Deprecated("use pipe elements")
object DataHandler {

    private val gson = Gson()

    fun extractDataModel(inputStream: InputStream, limit: Long = -1) =
            extractDataModel(inputStream.bufferedReader(), limit)

    fun extractDataModel(file: String, limit: Long = -1) =
            extractDataModel(BufferedReader(FileReader(file)), limit)

    fun extractDataModel(reader: BufferedReader, limit: Long = -1): DataModel {
        val model = DataModel()

        reader.lines()
                .limit(if (limit < 0) Long.MAX_VALUE else limit)
                .forEach { model.generalize(it) }

        return model
    }

    fun parseData(inputStream: InputStream, clazz: Class<*>, limit: Long = -1) =
            parseData(inputStream.bufferedReader(), clazz, limit)

    fun parseData(reader: BufferedReader, clazz: Class<*>, limit: Long = -1): DataRepository {
        var stream = reader.lines()

        if (limit > 0) {
            stream = stream.limit(limit)
        }

        val lst = stream.map { gson.fromJson(it, clazz) }.collect(Collectors.toList())
        return DataRepository(clazz, lst)
    }

}