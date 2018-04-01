package datapipe.core.data.model.metadata.dsl.builder

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.MetadataType

/**
 * @author: Andrei Shlykov
 * @since: 31.03.2018
 */
class MetadataClassBuilder {

    private val properties = mutableListOf<MetadataPropertyBuilder>()

    operator fun MetadataType.unaryPlus() {
        properties.add(MetadataPropertyBuilder("p${properties.size}") to this)
    }

    operator fun MetadataPropertyBuilder.unaryPlus() {
        properties.add(this)
    }

    operator fun String.unaryPlus(): MetadataPropertyBuilder {
        val builder = MetadataPropertyBuilder(this)
        properties.add(builder)
        return builder
    }

    infix fun String.to(type: MetadataType) = MetadataPropertyBuilder(this) to type
    infix fun String.or(alias: String) = MetadataPropertyBuilder(this) or alias

    fun build() = MetadataClass(properties.map(MetadataPropertyBuilder::build).toSet())

}