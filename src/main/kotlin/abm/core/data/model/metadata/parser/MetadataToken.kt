package abm.core.data.model.metadata.parser

import abm.core.data.model.metadata.*

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
abstract class MetadataAstNode(val parent: MetadataAstNode? = null) {

    init {
        parent?.children?.add(this)
    }

    val children = LinkedHashSet<MetadataAstNode>()

}

abstract class MetadataAstTypeNode(parent: MetadataAstNode): MetadataAstNode(parent)

class RootNode: MetadataAstNode() {

    val child: MetadataAstNode?
        get() = children.firstOrNull()
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val properties: Set<MetadataPropertyNode>
        get() = children as Set<MetadataPropertyNode>
}

class MetadataListNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val containedType: MetadataAstNode?
        get() = children.firstOrNull()
}

class MetadataPrimitiveNode(val type: MetadataPrimitive, parent: MetadataAstNode): MetadataAstTypeNode(parent)

class MetadataPropertyNode(parent: MetadataAstNode) : MetadataAstNode(parent) {

    val names: List<MetadataPropertyNameNode>
        get() = children.filter { it is MetadataPropertyNameNode } as List<MetadataPropertyNameNode>

    val type: MetadataAstNode?
        get() = children.firstOrNull { it is MetadataAstTypeNode }
}

class MetadataPropertyNameNode(val name: String, parent: MetadataAstNode): MetadataAstNode(parent)

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

            is PropertyNameToken -> {
                if (current is MetadataClassNode) {
                    val propNode = MetadataPropertyNode(current)
                    MetadataPropertyNameNode(token.name, propNode)
                } else {
                    throw RuntimeException("can't attach PropertyNameNode")
                }
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
    is MetadataClassNode -> MetadataClass(node.properties.map { buildMetadata(it) as PropertyMetadata }.toSet())
    is MetadataListNode -> MetadataList(buildMetadata(node.containedType!!) as MetadataType)
    is MetadataPropertyNode -> PropertyMetadata(
            node.names.first().name,
            buildMetadata(node.type!!) as MetadataType,
            node.names.drop(1).map(MetadataPropertyNameNode::name).toSet())
    is MetadataPrimitiveNode -> node.type

    else -> throw RuntimeException("can't process $node")
}