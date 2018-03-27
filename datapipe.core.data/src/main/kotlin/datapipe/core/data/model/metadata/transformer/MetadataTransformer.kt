package datapipe.core.data.model.metadata.transformer

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.model.metadata.transformer.operation.MetadataTransformOperation

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
class MetadataTransformer(private val operations: List<MetadataTransformOperation>) {

    fun transform(source: GeneratedClass, destination: GeneratedClass): GeneratedClass {
        for (operation in operations) {
            operation(source, destination)
        }
        return destination
    }

}