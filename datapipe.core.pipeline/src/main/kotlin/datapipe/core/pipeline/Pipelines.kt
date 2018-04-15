package datapipe.core.pipeline

import datapipe.core.data.model.DataRepository
import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.parser.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.model.metadata.Metadata
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.PrimitiveNull
import datapipe.core.data.model.metadata.parser.visitor.AliasForBadNameVisitor
import datapipe.core.data.model.metadata.parser.visitor.MetadataAstNodeVisitor
import datapipe.core.data.model.metadata.parser.visitor.RemovePropertiesByNameVisitor
import datapipe.core.data.model.metadata.parser.visitor.RemoveUnnecessaryPropertiesVisitor
import java.io.BufferedReader
import java.io.FileReader
import java.util.stream.Collectors

/**
 * @author: Andrei Shlykov
 * @since: 03.02.2018
 */
object Pipelines {

    fun extractModelFrom(path: String, limit: Long = -1): AbstractPipelineElement<Unit, Metadata> =
            ModelExtractor(path, limit)

    fun generateClass(): AbstractPipelineElement<Metadata, Class<GeneratedClass>> =
            PipelineElement { (it as MetadataClass).generatedClass }

    fun parseData(path: String, limit: Long = -1): AbstractPipelineElement<Class<GeneratedClass>, DataRepository> =
            DataParser(path, limit)

    fun aliasForBadNames(): AbstractPipelineElement<Metadata, Metadata> =
            MetadataVisitorConsumer(MetadataAstNode::levelOrderIterator, AliasForBadNameVisitor())

    fun removeUnnecessaryProperties(): AbstractPipelineElement<Metadata, Metadata> =
            MetadataVisitorConsumer(MetadataAstNode::postOrderIterator, RemoveUnnecessaryPropertiesVisitor())

    fun excludeNamesFromMetadata(vararg names: String): AbstractPipelineElement<Metadata, Metadata> =
            MetadataVisitorConsumer(MetadataAstNode::postOrderIterator, RemovePropertiesByNameVisitor(names.toSet()))

    fun <T> process(task: (T?) -> Unit): AbstractPipelineElement<T, T> = PipelineElement {
        task(it)
        it!!
    }

    private class DataParser(val path: String, val limit: Long)
        : AbstractPipelineElement<Class<GeneratedClass>, DataRepository>()
    {
        // TODO возможно стоит принимать Either<Class<GeneratedClass>, BrokenPipe>
        override fun performTask(param: Class<GeneratedClass>?): DataRepository {
            if (param == null) throw RuntimeException("param is null")

            val reader = BufferedReader(FileReader(path))
            val gson = Gson()

            var stream = reader.lines()
            if (limit > 0) {
                stream = stream.limit(limit)
            }
            val lst = stream.map { str -> gson.fromJson(str, param) }.collect(Collectors.toList())

            return DataRepository(param, lst)
        }
    }

    private class ModelExtractor(val path: String, val limit: Long)
        : AbstractPipelineElement<Unit, Metadata>()
    {
        override fun performTask(param: Unit?): Metadata {
            val reader = BufferedReader(FileReader(path))
            val parser = JsonParser()

            var stream = reader.lines()
            if (limit > 0) {
                stream = stream.limit(limit)
            }
            return stream
                    .map { constructMetadataFromJson(parser.parse(it)) }
                    .reduce(PrimitiveNull, MetadataType::combine)
        }
    }

    private class MetadataVisitorConsumer(val rootIterator: MetadataAstNode.() -> Iterator<MetadataAstNode>,
                                          val visitor: MetadataAstNodeVisitor)
        : AbstractPipelineElement<Metadata, Metadata>()
    {
        // TODO возможно стоит принимать Either<Class<GeneratedClass>, BrokenPipe>
        override fun performTask(param: Metadata?): Metadata {
            if (param == null) throw RuntimeException("param is null")

            return if (param is MetadataClass) {
                val root = buildMetadataAstTree(tokenize(param))
                root.rootIterator().forEach { it.visit(visitor) }
                buildMetadata(root)
            } else {
                // TODO По сути это ошибка, так как операция нацелена на MetadataClass
                // возможно стоит возвращать Either<MetadataClass, BrokenPipe>
                param
            }
        }
    }

}