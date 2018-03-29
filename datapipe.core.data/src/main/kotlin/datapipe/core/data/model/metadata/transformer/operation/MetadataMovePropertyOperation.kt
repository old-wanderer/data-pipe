package datapipe.core.data.model.metadata.transformer.operation

import datapipe.core.data.generator.GeneratedClass

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
class MetadataMovePropertyOperation(private val from: String, private val to: String): MetadataTransformOperation {

    override fun invoke(source: GeneratedClass, destination: GeneratedClass) {
        val value = source.getPropertyValue(from)
        destination.setPropertyValue(to, value)
    }
}