package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.parser.MetadataClassNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNode

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
class RemovePropertiesByNameVisitor(private val names: Set<String>): MetadataAstNodeVisitor {

    override fun visitMetadataClassNode(node: MetadataClassNode) {
        node.properties.removeIf { propertyNode ->
            propertyNode.names.any { it.name in names }
        }
    }

}