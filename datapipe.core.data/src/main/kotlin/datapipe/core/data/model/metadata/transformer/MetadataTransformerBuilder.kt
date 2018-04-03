package datapipe.core.data.model.metadata.transformer

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.parser.*
import datapipe.core.data.model.metadata.transformer.operation.MetadataMovePropertyOperation
import datapipe.core.data.model.metadata.transformer.operation.MetadataTransformOperation

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
class MetadataTransformerBuilder(sourceMetadata: MetadataClass) {

    private val operations = mutableListOf<MetadataTransformOperation>()
    private val root = buildMetadataAstTree(tokenize(sourceMetadata).toList()) as RootNode

    infix fun String.moveTo(destinationPropPath: String) {
        operations.add(MetadataMovePropertyOperation(this, destinationPropPath))

        val propertyNode = root.getPropertyNodeByPath(this)
        propertyNode.parent!!.children.remove(propertyNode)
        root.setPropertyNodePath(destinationPropPath, propertyNode)
    }

    fun build() = MetadataTransformer(buildMetadata(root) as MetadataClass, operations)
}

fun MetadataClass.transformTo(init: MetadataTransformerBuilder.() -> Unit): MetadataTransformer {
    val builder = MetadataTransformerBuilder(this)
    builder.init()
    return builder.build()
}

// TODO рефакторинг и перенести метод
private fun RootNode.setPropertyNodePath(path: String, propertyNode: MetadataPropertyNode) {
    if (path.isNotBlank()) {
        val parts = path.split(".")
        var currentClassNode = this.child as MetadataClassNode
        for (part in parts.dropLast(1)) {
            val nextNode = currentClassNode.properties
                    .find { it.names.any { part == it.name } }?.type as MetadataClassNode?

            currentClassNode = if (nextNode == null) {
                val newPropertyNode = MetadataPropertyNode(currentClassNode)
                MetadataPropertyNameNode(part, newPropertyNode)
                MetadataClassNode(newPropertyNode)
            } else {
                nextNode
            }
        }
        val newPropertyNode = MetadataPropertyNode(currentClassNode)
        MetadataPropertyNameNode(parts.last(), newPropertyNode)
        propertyNode.type!!.parent = newPropertyNode
        newPropertyNode.children.add(propertyNode.type!!)
        return
    }
    throw IllegalArgumentException("path is blank")
}

// TODO рефакторинг и перенести метод
private fun RootNode.getPropertyNodeByPath(path: String): MetadataPropertyNode {
    if (path.isNotBlank()) {
        val parts = path.split(".")
        var currentClassNode = this.child as MetadataClassNode
        for (part in parts.dropLast(1)) {
            currentClassNode = currentClassNode.properties
                    .find { it.names.any { part == it.name } }?.type as? MetadataClassNode
                    ?: throw RuntimeException("properties for such a path ($path) do not exist or an invalid type")
        }
        return currentClassNode.properties.find { it.names.any { parts.last() == it.name } }
                ?: throw RuntimeException("property for such a path ($path) do not exist")
    }
    throw IllegalArgumentException("path is blank")
}