package abm.core.data.pipeline

import abm.core.data.generator.ClassGenerator
import abm.core.data.handler.DataRepository
import abm.core.data.model.metadata.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.FileReader
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

                if (source is MetadataClass) {
                    buildMetadata(buildMetadataAstTree(metadataTokens(source).map { token ->
                        if (token is PropertyNameToken) {
                            if (token.name.isBadName()) {
                                return@map PropertyNameToken(token.name.correctBadName(), token.aliases + token.name)
                            }
                        }
                        token
                    }.toList()))
                } else {
                    source!!
                }
            }

    fun removeUnnecessaryProperties(): PipelineElement<Metadata, Metadata> =
            PipelineElement {
                if (it is MetadataClass) {
                    val ast = buildMetadataAstTree(metadataTokens(it).toList())
                    dfsMetadataAst(ast)
                    return@PipelineElement buildMetadata(ast)
                }
                it!!
            }

    private fun dfsMetadataAst(node: MetadataAstNode) {
        node.children.removeIf {
            dfsMetadataAst(it)
            unnecessaryPropertiesPredicateNode(it)
        }
    }

    private fun unnecessaryPropertiesPredicateNode(node: MetadataAstNode): Boolean = when (node) {
        is MetadataPropertyNode -> node.type == null
        is MetadataListNode -> node.containedType == null

        is MetadataClassNode -> node.properties.isEmpty()
        is MetadataPrimitiveNode -> node.type == PrimitiveNull

        else -> false
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

}