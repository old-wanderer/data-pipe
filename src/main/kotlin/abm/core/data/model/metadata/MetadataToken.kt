package abm.core.data.model.metadata

/**
 * @author: Andrei Shlykov
 * @since: 22.02.2018
 */
sealed class MetadataToken
data class PropertyToken(val prop: PropertyMetadata): MetadataToken()
data class PrimitiveToken(val type: MetadataPrimitive): MetadataToken()

data class PropertyNameToken(val name: String): MetadataToken()
//data class TypeToken(val type: Metadata): MetadataToken()


object ObjectBegin: MetadataToken()
object ObjectEnd: MetadataToken()
object ListBegin: MetadataToken()
object ListEnd: MetadataToken()


@Suppress("LeakingThis")
abstract class MetadataAstNode(val parent: MetadataAstNode? = null) {

    init {
        parent?.addChild(this)
    }

    abstract fun addChild(child: MetadataAstNode)

}

class RootNode: MetadataAstNode() {

    lateinit var child: MetadataAstNode

    override fun addChild(child: MetadataAstNode) {
        this.child = child
    }
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstNode(parent) {

    val children: MutableSet<MetadataPropertyNode> = mutableSetOf()

    override fun addChild(child: MetadataAstNode) {
        children.add(child as MetadataPropertyNode)
    }
}

class MetadataListNode(parent: MetadataAstNode): MetadataAstNode(parent) {

    lateinit var containedType: MetadataAstNode

    override fun addChild(child: MetadataAstNode) {
        containedType = child
    }
}

data class MetadataPrimitiveNode(val type: MetadataPrimitive): MetadataAstNode() {

    override fun addChild(child: MetadataAstNode) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class MetadataPropertyNode(val name: String, parent: MetadataAstNode): MetadataAstNode(parent) {

    lateinit var type: MetadataAstNode

    override fun addChild(child: MetadataAstNode) {
        type = child
    }
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
                    MetadataPropertyNode(token.name, current)
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

            else -> throw RuntimeException("can't process token $token")
        }
    }

    return root
}

fun buildMetadata(node: MetadataAstNode): Metadata = when (node) {
    is RootNode -> buildMetadata(node.child)
    is MetadataClassNode -> MetadataClass(node.children.map { buildMetadata(it) as PropertyMetadata }.toSet())
    is MetadataListNode  -> MetadataList(buildMetadata(node.containedType))
    is MetadataPropertyNode  -> PropertyMetadata(node.name, buildMetadata(node.type))
    is MetadataPrimitiveNode -> node.type

    else -> throw RuntimeException("can't process $node")
}