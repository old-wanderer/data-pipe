package datapipe.core.data.model.metadata.parser

import datapipe.core.common.Visited
import datapipe.core.data.model.metadata.Metadata
import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.MetadataList
import datapipe.core.data.model.metadata.MetadataPrimitive
import datapipe.core.data.model.metadata.MetadataProperty
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.parser.visitor.MetadataAstNodeVisitor
import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 22.02.2018
 */
sealed class MetadataToken
data class PrimitiveToken(val type: MetadataPrimitive): MetadataToken()
data class PropertyNameToken(val name: String): MetadataToken()

object TypeSeparator: MetadataToken()
object AliasSeparator: MetadataToken()
object EOFToken: MetadataToken()

object ObjectBegin: MetadataToken()
object ObjectEnd: MetadataToken()
object ListBegin: MetadataToken()
object ListEnd: MetadataToken()


@Suppress("LeakingThis")
abstract class MetadataAstNode(val parent: MetadataAstNode? = null)
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

abstract class MetadataAstTypeNode(parent: MetadataAstNode): MetadataAstNode(parent)

class RootNode: MetadataAstNode() {

    val child: MetadataAstNode?
        get() = children.firstOrNull()

    override fun visit(visitor: MetadataAstNodeVisitor) {}
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val properties: Set<MetadataPropertyNode>
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
        get() = children.filter { it is MetadataPropertyNameNode } as List<MetadataPropertyNameNode>

    val type: MetadataAstNode?
        get() = children.firstOrNull { it is MetadataAstTypeNode }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNode(this)
}

class MetadataPropertyNameNode(val name: String, parent: MetadataAstNode): MetadataAstNode(parent) {

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNameNode(this)
}

// ---------------------------------------------------------------------------------

fun buildMetadataAstTree(tokens: Iterable<MetadataToken>): MetadataAstNode {
    val root = RootNode()
    var current: MetadataAstNode = root

    for (token in tokens) {
        current = when (token) {
            ObjectBegin -> MetadataClassNode(current)
            ObjectEnd -> {
                if (current is MetadataClassNode) {
                    val parent = current.parent!!
                    if (parent is MetadataPropertyNode) {
                        parent.parent!!
                    } else {
                        parent
                    }
                } else {
                    throw RuntimeException("expected type MetadataClassNode")
                }
            }

            is PropertyNameToken -> when (current) {
                is MetadataClassNode -> {
                    val propNode = MetadataPropertyNode(current)
                    MetadataPropertyNameNode(token.name, propNode)
                }
                is MetadataPropertyNode -> MetadataPropertyNameNode(token.name, current)
                else -> throw RuntimeException("can't attach PropertyNameNode")
            }

            is PrimitiveToken -> {
                when (current) {
                    is MetadataPropertyNode -> {
                        current.children.add(MetadataPrimitiveNode(token.type, current))
                        current.parent!!
                    }
                    is MetadataListNode -> {
                        current.children.add(MetadataPrimitiveNode(token.type, current))
                        current
                    }
                    else -> throw RuntimeException("sddssd")
                }
            }

            ListBegin -> MetadataListNode(current)
            ListEnd -> {
                if (current is MetadataListNode) {
                    val parent = current.parent!!
                    if (parent is MetadataPropertyNode) {
                        parent.parent!!
                    } else {
                        parent
                    }
                } else {
                    throw RuntimeException("expected type MetadataListNode")
                }
            }

            TypeSeparator -> current.parent!!
            AliasSeparator -> current.parent!!
            EOFToken -> if (current is RootNode) current else throw RuntimeException("unexpected EOFToken")
        }
    }

    return root
}

fun buildMetadata(node: MetadataAstNode): Metadata = when (node) {
    is RootNode -> buildMetadata(node.child!!)
    is MetadataClassNode -> MetadataClass(node.properties.map { buildMetadata(it) as MetadataProperty }.toSet())
    is MetadataListNode -> MetadataList(buildMetadata(node.containedType!!) as MetadataType)
    is MetadataPropertyNode -> MetadataProperty(
            node.names.first().name,
            buildMetadata(node.type!!) as MetadataType,
            node.names.drop(1).map(MetadataPropertyNameNode::name).toSet())
    is MetadataPrimitiveNode -> node.type

    else -> throw RuntimeException("can't process $node")
}