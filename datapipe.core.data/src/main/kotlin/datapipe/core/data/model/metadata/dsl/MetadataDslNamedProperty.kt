package datapipe.core.data.model.metadata.dsl

import datapipe.core.data.model.metadata.MetadataType

/**
 * @author: Andrei Shlykov
 * @since: 01.04.2018
 */
interface MetadataDslNamedProperty {

    infix fun or(alias: String): MetadataDslNamedProperty

    infix fun to(type: MetadataType): MetadataDslTypedProperty

}