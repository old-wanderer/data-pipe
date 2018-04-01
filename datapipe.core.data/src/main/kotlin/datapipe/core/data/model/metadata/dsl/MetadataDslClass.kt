package datapipe.core.data.model.metadata.dsl

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.dsl.builder.MetadataPropertyBuilder

/**
 * @author: Andrei Shlykov
 * @since: 01.04.2018
 */
interface MetadataDslClass {

    operator fun MetadataType.unaryPlus()

    operator fun String.unaryPlus(): MetadataDslNamedProperty

    operator fun MetadataDslTypedProperty.unaryPlus()

    infix fun String.to(type: MetadataType): MetadataDslTypedProperty = MetadataPropertyBuilder(this) to type
    infix fun String.or(alias: String): MetadataDslNamedProperty = MetadataPropertyBuilder(this) or alias

    fun build(): MetadataClass

}