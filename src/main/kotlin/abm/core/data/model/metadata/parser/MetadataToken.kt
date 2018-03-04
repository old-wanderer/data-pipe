package abm.core.data.model.metadata.parser

import abm.core.data.model.metadata.*

/**
 * @author: Andrei Shlykov
 * @since: 22.02.2018
 */
sealed class MetadataToken
data class PrimitiveToken(val type: MetadataPrimitive): MetadataToken()
data class PropertyNameToken(val name: String, val aliases: Set<String> = emptySet()): MetadataToken()

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
        parent?.addChild(this)
    }

    val children = mutableSetOf<MetadataAstNode>()

    @Deprecated("use children.add(child)")
    fun addChild(child: MetadataAstNode) {
        children.add(child)
    }

}

class RootNode: MetadataAstNode() {

    val child: MetadataAstNode?
        get() = children.firstOrNull()
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstNode(parent) {

    val properties: Set<MetadataPropertyNode>
        get() = children as Set<MetadataPropertyNode>
}

class MetadataListNode(parent: MetadataAstNode): MetadataAstNode(parent) {

    val containedType: MetadataAstNode?
        get() = children.firstOrNull()
}

data class MetadataPrimitiveNode(val type: MetadataPrimitive): MetadataAstNode()

class MetadataPropertyNode(val name: String,
                           parent: MetadataAstNode,
                           val aliases: Set<String> = setOf()) : MetadataAstNode(parent) {

    val type: MetadataAstNode?
        get() = children.firstOrNull()
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

            is PropertyNameToken -> {
                if (current is MetadataClassNode) {
                    MetadataPropertyNode(token.name, current, token.aliases)
                } else {
                    throw RuntimeException("can't attach PropertyNameNode")
                }
            }

            is PrimitiveToken -> {
                when (current) {
                    is MetadataPropertyNode -> {
                        current.addChild(MetadataPrimitiveNode(token.type))
                        current.parent!!
                    }
                    is MetadataListNode -> {
                        current.addChild(MetadataPrimitiveNode(token.type))
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

            // TODO передалть построение ast
            else -> throw RuntimeException("unexpected token $token")
        }
    }

    return root
}

fun buildMetadata(node: MetadataAstNode): Metadata = when (node) {
    is RootNode -> buildMetadata(node.child!!)
    is MetadataClassNode -> MetadataClass(node.properties.map { buildMetadata(it) as PropertyMetadata }.toSet())
    is MetadataListNode -> MetadataList(buildMetadata(node.containedType!!) as MetadataType)
    is MetadataPropertyNode -> PropertyMetadata(node.name, buildMetadata(node.type!!) as MetadataType, node.aliases)
    is MetadataPrimitiveNode -> node.type

    else -> throw RuntimeException("can't process $node")
}