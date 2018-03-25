package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.PrimitiveNull
import datapipe.core.data.model.metadata.parser.*

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
// TODO расширать предикат для удаления
class RemoveUnnecessaryPropertiesVisitor: MetadataAstNodeVisitor {

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