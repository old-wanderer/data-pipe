package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.PrimitiveNull
import datapipe.core.data.model.metadata.parser.*

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
class RemoveUnnecessaryPropertiesVisitor(private val predicate: (MetadataAstNode) -> Boolean = isUnnecessaryNode)
    : MetadataAstNodeVisitor {

    companion object {
        val isUnnecessaryNode: (MetadataAstNode) -> Boolean = { node ->
            when (node) {
                is MetadataPropertyNode -> node.type == null
                is MetadataListNode -> node.containedType == null
                is MetadataClassNode -> node.properties.isEmpty()
                is MetadataPrimitiveNode -> node.type == PrimitiveNull
                else -> false
            }
        }
    }

    override fun visitMetadataClassNode(node: MetadataClassNode) {
        node.properties.removeIf { predicate(it) }
    }

    override fun visitMetadataListNode(node: MetadataListNode) {
        if (predicate(node.containedType!!)) {
            node.containedType = null
        }
    }

    override fun visitMetadataPropertyNode(node: MetadataPropertyNode) {
        if (predicate(node.name!!)) {
            node.name = null
        }
        if (predicate(node.type!!)) {
            node.type = null
        }
    }
}