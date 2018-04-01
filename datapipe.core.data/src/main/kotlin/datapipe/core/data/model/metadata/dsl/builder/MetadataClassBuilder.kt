package datapipe.core.data.model.metadata.dsl.builder

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.MetadataType
import datapipe.core.data.model.metadata.dsl.MetadataDslClass
import datapipe.core.data.model.metadata.dsl.MetadataDslNamedProperty
import datapipe.core.data.model.metadata.dsl.MetadataDslTypedProperty

/**
 * @author: Andrei Shlykov
 * @since: 31.03.2018
 */
class MetadataClassBuilder: MetadataDslClass {

    private val properties = mutableListOf<MetadataDslTypedProperty>()

    override operator fun MetadataType.unaryPlus() {
        properties.add(MetadataPropertyBuilder("p${properties.size}") to this)
    }

    override operator fun MetadataDslTypedProperty.unaryPlus() {
        properties.add(this)
    }

    override operator fun String.unaryPlus(): MetadataDslNamedProperty {
        val builder = MetadataPropertyBuilder(this)
        properties.add(builder)
        return builder
    }

    override fun build() = MetadataClass(properties.map(MetadataDslTypedProperty::build).toSet())

}