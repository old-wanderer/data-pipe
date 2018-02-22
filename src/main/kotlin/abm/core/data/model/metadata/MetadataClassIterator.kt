package abm.core.data.model.metadata

import java.util.*

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class MetadataClassIterator(metadataClass: MetadataClass): Iterator<MetadataToken> {

    private val stack = LinkedList<MetadataToken>(
            listOf(ObjectBegin, *(metadataClass.properties.map(::PropertyToken).toTypedArray()), ObjectEnd))

    override fun hasNext() = stack.isNotEmpty()

    override fun next(): MetadataToken {
        val token = stack.poll()
        if (token is PropertyToken) {
            when(token.prop.type) {
                is MetadataClass -> propertiesToTokens(token.prop.type).forEach(stack::push)
                is MetadataList -> pushMetadataListPropertiesIfExist(token.prop.type)
            }
        }
        return token
    }

    private fun propertiesToTokens(metadataClass: MetadataClass) =
            listOf(ObjectBegin, *(metadataClass.properties.map(::PropertyToken).toTypedArray()), ObjectEnd).reversed()

    private fun pushMetadataListPropertiesIfExist(metadataList: MetadataList) {
        stack.push(ListEnd)
        when(metadataList.containsType) {
            is MetadataPrimitive -> stack.push(PrimitiveToken(metadataList.containsType))
            is MetadataList -> pushMetadataListPropertiesIfExist(metadataList.containsType)
            is MetadataClass -> propertiesToTokens(metadataList.containsType).forEach(stack::push)
        }
        stack.push(ListBegin)
    }

}