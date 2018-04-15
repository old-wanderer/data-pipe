package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.MetadataPrimitive

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

// ---------------------------------------------------------------------------------

fun buildMetadataAstTree(tokens: Sequence<MetadataToken>) = buildMetadataAstTree(tokens.iterator())

fun buildMetadataAstTree(tokens: Iterable<MetadataToken>) = buildMetadataAstTree(tokens.iterator())

fun buildMetadataAstTree(tokens: Iterator<MetadataToken>): MetadataAstNode {
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
                // todo test for build metadata with aliases
                is MetadataPropertyNode -> MetadataPropertyNameNode(token.name, current)
                is MetadataPropertyNameNode -> MetadataPropertyNameNode(token.name, current)
                else -> throw RuntimeException("can't attach PropertyNameNode")
            }

            is PrimitiveToken -> {
                when (current) {
                    is MetadataPropertyNode -> {
                        current.addChild(MetadataPrimitiveNode(token.type, current))
                        current.parent!!
                    }
                    is MetadataListNode -> {
                        current.addChild(MetadataPrimitiveNode(token.type, current))
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

            TypeSeparator -> {
                if (current is MetadataPropertyNameNode) {
                    var propNode = current
                    while (propNode !is MetadataPropertyNode) {
                        propNode = propNode.parent!!
                    }
                    propNode
                } else {
                    throw RuntimeException("expected MetadataPropertyNameNode before TypeSeparator")
                }
            }
            AliasSeparator -> current
            EOFToken -> if (current is RootNode) current else throw RuntimeException("unexpected EOFToken")
        }
    }

    return root
}