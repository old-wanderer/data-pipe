package datapipe.core.data.model.metadata.transformer

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.transformer.operation.MetadataTransformOperation

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
class MetadataTransformer(val destinationMetadata: MetadataClass,
                          private val operations: List<MetadataTransformOperation>) {

    private val destinationDefaultConstructor = destinationMetadata.generatedClass.getDeclaredConstructor()

    fun transform(source: GeneratedClass): GeneratedClass {
        val destination = destinationDefaultConstructor.newInstance()
        for (operation in operations) {
            operation(source, destination)
        }
        return destination
    }

}