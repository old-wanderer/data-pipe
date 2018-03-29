package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.common.PredicateProcessor
import datapipe.core.data.model.metadata.parser.MetadataAstNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNameNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNode

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
private typealias VisitorPredicate = (String) -> Boolean
private typealias VisitorProcessor = (String) -> String

class AliasForBadNameVisitor(private val predicateProcessor: PredicateProcessor<String, String> =
                                     PredicateProcessor(defaultPredicate, defaultProcessor))
    : MetadataAstNodeVisitor {

    constructor(vararg ops: Pair<VisitorPredicate, VisitorProcessor>): this(PredicateProcessor(ops.toList()))

    companion object {
        val defaultPredicate: VisitorPredicate =
                { parameter -> !parameter.first().isJavaIdentifierStart() || !parameter.all(Char::isJavaIdentifierPart) }
        val defaultProcessor: VisitorProcessor = { parameter -> buildString {
            val firstCharIndex = parameter.indexOfFirst(Char::isJavaIdentifierStart)
            append(parameter[firstCharIndex])
            append(parameter.drop(firstCharIndex+1).filter(Char::isJavaIdentifierPart))
        }}
    }

    override fun visitMetadataPropertyNode(node: MetadataPropertyNode) {
        val nameNode = node.names.first()
        var name = nameNode.name
        while (true) {
            val processor = predicateProcessor.findProcessor(name) ?: break
            name = processor(name)
        }
        if (nameNode.name != name) {
            // пересобираются, так как надо поддерживать порядок
            // говнокод какой-то :( // FIXME
            val newChildren = LinkedHashSet<MetadataAstNode>()
            newChildren.add(MetadataPropertyNameNode(name, node))
            node.children.remove(nameNode)
            newChildren.addAll(node.children)
            newChildren.add(MetadataPropertyNameNode(nameNode.name, node))
            node.children.removeIf { true }
            node.children.addAll(newChildren)
        }
    }
}