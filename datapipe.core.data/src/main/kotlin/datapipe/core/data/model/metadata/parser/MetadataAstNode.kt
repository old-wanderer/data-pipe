package datapipe.core.data.model.metadata.parser

import datapipe.core.common.Visited
import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.parser.visitor.MetadataAstNodeVisitor
import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 13.04.2018
 */
@Suppress("LeakingThis")
sealed class MetadataAstNode(var parent: MetadataAstNode? = null)
    : Visited<MetadataAstNodeVisitor>, Iterable<MetadataAstNode> {

    init {
        parent?.children?.add(this)
    }

    val children = LinkedHashSet<MetadataAstNode>()

    override fun iterator(): Iterator<MetadataAstNode> = BfsTreeIterator(this)

    fun levelOrderIterator(): Iterator<MetadataAstNode> = BfsTreeIterator(this)

    fun postOrderIterator(): Iterator<MetadataAstNode> = buildSequence {
        for (child in children) {
            yieldAll(child.postOrderIterator().asSequence().toList())
        }
        yield(this@MetadataAstNode)
    }.iterator()

}

sealed class MetadataAstTypeNode(parent: MetadataAstNode): MetadataAstNode(parent)

class RootNode: MetadataAstNode() {

    val child: MetadataAstNode?
        get() = children.firstOrNull()

    override fun visit(visitor: MetadataAstNodeVisitor) {}
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val properties: Set<MetadataPropertyNode>
        @Suppress("UNCHECKED_CAST")
        get() = children as Set<MetadataPropertyNode>

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataClassNode(this)
}

class MetadataListNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val containedType: MetadataAstNode?
        get() = children.firstOrNull()

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataListNode(this)
}

class MetadataPrimitiveNode(val type: MetadataPrimitive, parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPrimitiveTypeNode(this)
}

class MetadataPropertyNode(parent: MetadataAstNode) : MetadataAstNode(parent) {

    val names: List<MetadataPropertyNameNode>
        @Suppress("UNCHECKED_CAST")
        get() = children.filter { it is MetadataPropertyNameNode } as List<MetadataPropertyNameNode>

    val type: MetadataAstNode?
        get() = children.firstOrNull { it is MetadataAstTypeNode }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNode(this)
}

class MetadataPropertyNameNode(val name: String, parent: MetadataAstNode): MetadataAstNode(parent) {

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNameNode(this)
}

// ---------------------------------------------------------------------------------

fun buildMetadata(node: MetadataAstNode): Metadata = when (node) {
    is RootNode -> buildMetadata(node.child!!)
    is MetadataClassNode -> MetadataClass(node.properties.map { buildMetadata(it) as MetadataProperty }.toSet())
    is MetadataListNode -> MetadataList(buildMetadata(node.containedType!!) as MetadataType)
    is MetadataPropertyNode -> MetadataProperty(
            node.names.first().name,
            buildMetadata(node.type!!) as MetadataType,
            node.names.drop(1).map(MetadataPropertyNameNode::name).toSet())
    is MetadataPrimitiveNode -> node.type
    is MetadataPropertyNameNode -> throw RuntimeException("can't process MetadataPropertyNameNode")
}