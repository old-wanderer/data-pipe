package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.parser.MetadataClassNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNode

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
class RemovePropertiesByNameVisitor(val names: Set<String>): MetadataAstNodeVisitor {

    override fun visitMetadataClassNode(node: MetadataClassNode) {
        node.children.removeIf { propertyNode ->
            (propertyNode as MetadataPropertyNode).names.any { it.name in names }
        }
    }

}