package datapipe.core.data.model.metadata.transformer.operation

import datapipe.core.data.generator.GeneratedClass

/**
 * @author: Andrei Shlykov
 * @since: 27.03.2018
 */
interface MetadataTransformOperation {

    operator fun invoke(source: GeneratedClass, destination: GeneratedClass)

}