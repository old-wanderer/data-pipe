package abm.core.data.model.metadata.parser


/**
 * @author: Andrei Shlykov
 * @since: 05.03.2018
 */
interface MetadataAstNodeVisitor {

    fun visitMetadataClassNode(node: MetadataClassNode) = pass()

    fun visitMetadataListNode(node: MetadataListNode) = pass()

    fun visitMetadataPrimitiveTypeNode(node: MetadataPrimitiveNode) = pass()

    fun visitMetadataPropertyNode(node: MetadataPropertyNode) = pass()

    fun visitMetadataPropertyNameNode(node: MetadataPropertyNameNode) = pass()

    private fun pass() {}

}