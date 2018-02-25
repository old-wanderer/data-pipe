package abm.core.data.model.metadata

import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */


fun metadataTokens(metadataClass: MetadataClass): Sequence<MetadataToken> = buildSequence {
    yield(ObjectBegin)
    for (property in metadataClass.properties) {
        yield(PropertyNameToken(property.name))
        when (property.type) {
            is MetadataPrimitive -> yield(PrimitiveToken(property.type))
            is MetadataClass -> yieldAll(metadataTokens(property.type))
            is MetadataList  -> {
                yield(ListBegin)
                when (property.type.containsType) {
                    is MetadataPrimitive -> yield(PrimitiveToken(property.type.containsType))
                    is MetadataClass -> yieldAll(metadataTokens(property.type.containsType))
                    else -> throw RuntimeException("MetadataList contains unresolved type")
                }
                yield(ListEnd)
            }
        }
    }
    yield(ObjectEnd)
}