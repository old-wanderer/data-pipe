package datapipe.core.data.model.metadata.dsl.builder

import datapipe.core.data.model.metadata.MetadataProperty
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.dsl.MetadataDslNamedProperty
import datapipe.core.data.model.metadata.dsl.MetadataDslTypedProperty

/**
 * @author: Andrei Shlykov
 * @since: 31.03.2018
 */
class MetadataPropertyBuilder(name: String, vararg names: String)
    : MetadataDslNamedProperty, MetadataDslTypedProperty {

    private val names = mutableListOf(name, *names)
    private lateinit var type: MetadataType

    override infix fun or(alias: String): MetadataDslNamedProperty {
        names.add(alias)
        return this
    }

    override infix fun to(type: MetadataType): MetadataDslTypedProperty {
        this.type = type
        return this
    }

    override fun build(): MetadataProperty {
        if (!::type.isInitialized)
            throw RuntimeException("property ${names.first()}")
        return MetadataProperty(names.first(), type, names.drop(1).toSet())
    }

}