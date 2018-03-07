package datapipe.core.data.model.metadata

/**
 * @author: andrei shlykov
 * @since: 20.01.2018
 */
class TypesNotCombineException(typeA: Metadata, typeB: Metadata):
        RuntimeException("$typeA and $typeB")