package datapipe.core.data.model.metadata.dsl.builder

import datapipe.core.data.model.metadata.MetadataProperty
import datapipe.core.data.model.metadata.MetadataType

/**
 * @author: Andrei Shlykov
 * @since: 31.03.2018
 */
class MetadataPropertyBuilder(vararg names: String) {

    private val names = mutableListOf(*names)
    private lateinit var type: MetadataType

    infix fun or(alias: String): MetadataPropertyBuilder {
        names.add(alias)
        return this
    }

    infix fun to(type: MetadataType): MetadataPropertyBuilder {
        this.type = type
        return this
    }

    fun build() = MetadataProperty(names.first(), type, names.drop(1).toSet())

}