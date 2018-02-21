package abm.core.data.model.metadata

import java.util.*

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class PropertiesMetadataClassIterator(metadataClass: MetadataClass): Iterator<PropertyMetadata> {

    private val stack = LinkedList<PropertyMetadata>(metadataClass.properties)

    override fun hasNext() = stack.isNotEmpty()

    override fun next(): PropertyMetadata {
        val property = stack.poll()
        when(property.type) {
            is MetadataClass -> property.type.properties.reversed().forEach(stack::push)
            is MetadataList -> pushMetadataListPropertiesIfExist(property.type)
        }
        return property
    }

    private fun pushMetadataListPropertiesIfExist(metadataList: MetadataList) {
        when(metadataList.containsType) {
            is MetadataList -> pushMetadataListPropertiesIfExist(metadataList.containsType)
            is MetadataClass -> metadataList.containsType.properties.reversed().forEach(stack::push)
        }
    }

}