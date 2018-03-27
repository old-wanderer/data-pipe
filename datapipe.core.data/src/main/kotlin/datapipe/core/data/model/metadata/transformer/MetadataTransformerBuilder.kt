package datapipe.core.data.model.metadata.transformer

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.transformer.operation.MetadataMovePropertyOperation
import datapipe.core.data.model.metadata.transformer.operation.MetadataTransformOperation

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
class MetadataTransformerBuilder {

    private val operations = mutableListOf<MetadataTransformOperation>()

    infix fun String.moveTo(destinationPropPath: String) {
        operations.add(MetadataMovePropertyOperation(this, destinationPropPath))
    }

    fun build() = MetadataTransformer(operations)
}

fun MetadataClass.transformTo(init: MetadataTransformerBuilder.() -> Unit): MetadataTransformer {
    val builder = MetadataTransformerBuilder()
    builder.init()
    return builder.build()
}