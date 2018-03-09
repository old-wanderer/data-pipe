package datapipe.core.data.pipeline

import datapipe.core.data.generator.ClassGenerator
import datapipe.core.data.handler.DataRepository
import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.parser.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.model.metadata.Metadata
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.PrimitiveNull
import java.io.BufferedReader
import java.io.FileReader
import java.util.stream.Collectors
import kotlin.collections.LinkedHashSet

/**
 * @author: Andrei Shlykov
 * @since: 03.02.2018
 */
object Pipelines {

    fun extractModelFrom(path: String, limit: Long = -1): PipelineElement<Unit, Metadata> =
            ModelExtractor(path, limit)

    fun generateClass(): PipelineElement<Metadata, Class<GeneratedClass>> =
            PipelineElement { ClassGenerator.generateClass(it as MetadataClass) }

    fun parseData(path: String, limit: Long = -1): PipelineElement<Class<GeneratedClass>, DataRepository> =
            DataParser(path, limit)

    fun aliasForBadNames(): PipelineElement<Metadata, Metadata> =
            PipelineElement { source ->
                if (source is MetadataClass) {
                    val root = buildMetadataAstTree(tokenize(source).toList())
                    val visitor = AliasForBadNameVisitor()
                    root.forEach { it.visit(visitor) }
                    buildMetadata(root)
                } else {
                    source!!
                }
            }

    fun removeUnnecessaryProperties(): PipelineElement<Metadata, Metadata> =
            PipelineElement { source ->
                if (source is MetadataClass) {
                    val root = buildMetadataAstTree(tokenize(source).toList())
                    val visitor = RemoveUnnecessaryPropertiesVisitor()
                    root.postOrderIterator().forEach { it.visit(visitor) }
                    buildMetadata(root)
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
        :PipelineElement<Class<GeneratedClass>, DataRepository>
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
                .reduce(PrimitiveNull, MetadataType::combine)
    })

    // TODO возможность задать isBadName, correctBadName и обобщить механиз реконструкции метадаты
    // по умаолчанию должно проверять корректность индетификатора в java
    private class AliasForBadNameVisitor: MetadataAstNodeVisitor {

        fun String.isBadName() = this[0] == '/'
        fun String.correctBadName() = this.substring(1)

        override fun visitMetadataPropertyNode(node: MetadataPropertyNode) {
            val nameNode = node.names.first()
            if (nameNode.name.isBadName()) {
                // пересобираются, так как надо поддерживать порядок
                // говнокод какой-то :( // FIXME
                val newChildren = LinkedHashSet<MetadataAstNode>()
                newChildren.add(MetadataPropertyNameNode(nameNode.name.correctBadName(), node))
                node.children.remove(nameNode)
                newChildren.addAll(node.children)
                newChildren.add(MetadataPropertyNameNode(nameNode.name, node))
                node.children.removeIf { true }
                node.children.addAll(newChildren)
            }
        }
    }

    // TODO расширать предикат для удаления
    private class RemoveUnnecessaryPropertiesVisitor: MetadataAstNodeVisitor {

        private fun isUnnecessaryNode(node: MetadataAstNode) = when (node) {
            is MetadataPropertyNode -> node.type == null
            is MetadataListNode -> node.containedType == null
            is MetadataClassNode -> node.properties.isEmpty()
            is MetadataPrimitiveNode -> node.type == PrimitiveNull
            else -> false
        }

        override fun visitMetadataClassNode(node: MetadataClassNode) {
            node.children.removeIf(this::isUnnecessaryNode)
        }

        override fun visitMetadataListNode(node: MetadataListNode) {
            node.children.removeIf(this::isUnnecessaryNode)
        }

        override fun visitMetadataPropertyNode(node: MetadataPropertyNode) {
            node.children.removeIf(this::isUnnecessaryNode)
        }
    }

}