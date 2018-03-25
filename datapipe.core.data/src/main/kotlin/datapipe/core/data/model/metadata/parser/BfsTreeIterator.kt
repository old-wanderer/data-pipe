package datapipe.core.data.model.metadata.parser

import java.util.*

/**
 * @author: Andrei Shlykov
 * @since: 05.03.2018
 */
class BfsTreeIterator(node: MetadataAstNode): Iterator<MetadataAstNode> {

    private val stack = LinkedList<MetadataAstNode>(Collections.singleton(node))

    override fun hasNext() = stack.isNotEmpty()

    override fun next(): MetadataAstNode {
        val node = stack.poll()
        stack.addAll(node.children)
        return node
    }
}