package datapipe.core.data.model.metadata.dsl

import datapipe.core.data.model.metadata.MetadataProperty

/**
 * @author: Andrei Shlykov
 * @since: 01.04.2018
 */
interface MetadataDslTypedProperty {

    fun build(): MetadataProperty

}