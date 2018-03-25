package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.parser.MetadataAstNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNameNode
import datapipe.core.data.model.metadata.parser.MetadataPropertyNode

/**
 * @author: Andrei Shlykov
 * @since: 24.03.2018
 */
// TODO возможность задать isBadName, correctBadName и обобщить механиз реконструкции метадаты
// по умаолчанию должно проверять корректность индетификатора в java
class AliasForBadNameVisitor: MetadataAstNodeVisitor {

    private fun String.isBadName() = this[0] == '/'
    private fun String.correctBadName() = this.substring(1)

    override fun visitMetadataPropertyNode(node: MetadataPropertyNode) {
        val nameNode = node.names.first()
        if (nameNode.name.isBadName()) {
            // пересобираются, так как надо поддерживать порядок
            // говнокод какой-то :( // FIXME
            val newChildren = LinkedHashSet<MetadataAstNode>()
            newChildren.add(MetadataPropertyNameNode(nameNode.name.correctBadName(), node))
            node.children.remove(nameNode)
            newChildren.addAll(node.children)
            newChildren.add(MetadataPropertyNameNode(nameNode.name, node))
            node.children.removeIf { true }
            node.children.addAll(newChildren)
        }
    }
}