package datapipe.core.data.model.metadata

/**
 * @author: andrei shlykov
 * @since: 20.01.2018
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

fun metadataClass(init: MetadataClassBuilder.() -> Unit): MetadataClass {
    val clazz = MetadataClassBuilder()
    clazz.init()
    return clazz.build()
}

fun metadataList(containsType: MetadataType = PrimitiveNull) = MetadataList(containsType)