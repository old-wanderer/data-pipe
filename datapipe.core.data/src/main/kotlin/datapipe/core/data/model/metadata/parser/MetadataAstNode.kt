package datapipe.core.data.model.metadata.parser

import datapipe.core.common.Visited
import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.parser.visitor.MetadataAstNodeVisitor
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 13.04.2018
 */
@Suppress("LeakingThis") // todo разобраться
sealed class MetadataAstNode(var parent: MetadataAstNode? = null)
    : Visited<MetadataAstNodeVisitor>, Iterable<MetadataAstNode> {

    init {
        parent?.addChild(this)
    }

    abstract val children: List<MetadataAstNode>

    abstract fun addChild(child: MetadataAstNode)

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

    override val children: List<MetadataAstNode>
        get() = listOfNotNull(child)

    var child: MetadataAstNode? = null
        set(value) {
            if (field?.parent == this) {
                field?.parent = null
            }
            field = value
            value?.parent = this
        }

    override fun addChild(child: MetadataAstNode) {
        this.child = child
    }

    override fun visit(visitor: MetadataAstNodeVisitor) {}
}

class MetadataClassNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    val properties = mutableSetOf<MetadataPropertyNode>()

    override val children: List<MetadataAstNode>
        get() = properties.toList()

    override fun addChild(child: MetadataAstNode) {
        require(child is MetadataPropertyNode)
        properties.add(child as MetadataPropertyNode)
        child.parent = this
    }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataClassNode(this)
}

class MetadataListNode(parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    var containedType: MetadataAstTypeNode? = null
        set(value) {
            if (field?.parent == this) {
                field?.parent = null
            }
            field = value
            value?.parent = this
        }

    override val children: List<MetadataAstNode>
        get() = listOfNotNull(containedType)

    override fun addChild(child: MetadataAstNode) {
        require(child is MetadataAstTypeNode)
        containedType = child as MetadataAstTypeNode
    }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataListNode(this)
}

class MetadataPrimitiveNode(val type: MetadataPrimitive, parent: MetadataAstNode): MetadataAstTypeNode(parent) {

    override val children: List<MetadataAstNode>
        get() = listOf()

    override fun addChild(child: MetadataAstNode) = throw RuntimeException("can not add child to MetadataPrimitiveNode")

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPrimitiveTypeNode(this)
}

class MetadataPropertyNode(parent: MetadataAstNode) : MetadataAstNode(parent) {

    override val children: List<MetadataAstNode>
        get() = listOfNotNull(name, type)

    var name: MetadataPropertyNameNode? = null
        set(value) {
            if (field?.parent == this) {
                field?.parent = null
            }
            field = value
            value?.parent = this
        }

    var type: MetadataAstTypeNode? = null
        set(value) {
            if (field?.parent == this) {
                field?.parent = null
            }
            field = value
            value?.parent = this
        }

    val names: List<MetadataPropertyNameNode>
        get() = listOfNotNull(name, *aliases.toTypedArray())

    val aliases: List<MetadataPropertyNameNode>
        get() = Stream.iterate(name?.alias) { it?.alias }
                .takeWhile { it != null }
                .collect(Collectors.toList<MetadataPropertyNameNode>())

    override fun addChild(child: MetadataAstNode) {
        when (child) {
            is MetadataAstTypeNode -> type = child
            is MetadataPropertyNameNode -> name = child
            else -> throw RuntimeException("can not add child of type ${child.javaClass.simpleName} to MetadataPropertyNode")
        }
    }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNode(this)
}

class MetadataPropertyNameNode(val name: String, parent: MetadataAstNode): MetadataAstNode(parent) {

    var alias: MetadataPropertyNameNode? = null
        set(value) {
            if (field?.parent == this) {
                field?.parent = null
            }
            field = value
            value?.parent = this
        }

    override val children: List<MetadataAstNode>
        get() = listOfNotNull(alias)

    override fun addChild(child: MetadataAstNode) {
        require(child is MetadataPropertyNameNode)
        alias = child as MetadataPropertyNameNode
    }

    override fun visit(visitor: MetadataAstNodeVisitor) = visitor.visitMetadataPropertyNameNode(this)
}

// ---------------------------------------------------------------------------------

fun buildMetadata(node: MetadataAstNode): Metadata = when (node) {
    is RootNode -> buildMetadata(node.child!!)
    is MetadataClassNode -> MetadataClass(node.properties.map { buildMetadata(it) as MetadataProperty }.toSet())
    is MetadataListNode -> MetadataList(buildMetadata(node.containedType!!) as MetadataType)
    is MetadataPropertyNode -> MetadataProperty(
            node.name!!.name,
            buildMetadata(node.type!!) as MetadataType,
            node.aliases.map(MetadataPropertyNameNode::name).toSet())
    is MetadataPrimitiveNode -> node.type
    is MetadataPropertyNameNode -> throw RuntimeException("can't process MetadataPropertyNameNode")
}