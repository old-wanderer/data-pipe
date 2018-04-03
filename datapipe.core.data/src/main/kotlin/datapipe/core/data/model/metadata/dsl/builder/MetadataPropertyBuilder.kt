package datapipe.core.data.model.metadata.dsl.builder

import datapipe.core.data.model.metadata.MetadataProperty
import datapipe.core.data.model.metadata.MetadataPropertyDefaultValue
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
    private lateinit var defaultValue: Any

    override infix fun or(alias: String): MetadataDslNamedProperty {
        names.add(alias)
        return this
    }

    override infix fun to(type: MetadataType): MetadataDslTypedProperty {
        this.type = type
        return this
    }

    // TODO тесты генерации метаданных и класов с значением по умолчанию
    override fun default(value: Any): MetadataDslTypedProperty {
        this.defaultValue = value
        return this
    }

    override fun build(): MetadataProperty {
        // TODO тест на неицеализированный тип
        if (!::type.isInitialized)
            throw RuntimeException("property ${names.first()}")
        return if (!::defaultValue.isInitialized) {
            MetadataProperty(names.first(), type, names.drop(1).toSet())
        } else {
            MetadataPropertyDefaultValue(names.first(), type, defaultValue, names.drop(1).toSet())
        }
    }

}