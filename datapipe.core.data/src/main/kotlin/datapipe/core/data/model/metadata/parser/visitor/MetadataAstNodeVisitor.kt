package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.parser.*


/**
 * @author: Andrei Shlykov
 * @since: 05.03.2018
 */
interface MetadataAstNodeVisitor {

    fun visitMetadataClassNode(node: MetadataClassNode) = Unit

    fun visitMetadataListNode(node: MetadataListNode) = Unit

    fun visitMetadataPrimitiveTypeNode(node: MetadataPrimitiveNode) = Unit

    fun visitMetadataPropertyNode(node: MetadataPropertyNode) = Unit

    fun visitMetadataPropertyNameNode(node: MetadataPropertyNameNode) = Unit

}