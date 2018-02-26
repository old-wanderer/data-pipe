package abm.core.data.model.metadata

/**
 * @author: andrei shlykov
 * @since: 20.01.2018
 */

class MetadataClassBuilder {

    private val properties = mutableSetOf<PropertyMetadata>()

    operator fun MetadataType.unaryPlus() {
        properties.add(PropertyMetadata("p${properties.size}", this))
    }

    @JvmName("addProperty")
    operator fun Pair<String, MetadataType>.unaryPlus() {
        properties.add(PropertyMetadata(this.first, this.second))
    }

    @JvmName("addPropertyWithAlias")
    operator fun Pair<List<String>, MetadataType>.unaryPlus() {
        properties.add(PropertyMetadata(this.first.first(), this.second, this.first.drop(1).toSet()))
    }

    infix fun String.or(alias: String): List<String> = listOf(this, alias)
    infix fun List<String>.or(alias: String): List<String> = this + alias

    fun build() = MetadataClass(properties)

}

fun metadataClass(init: MetadataClassBuilder.() -> Unit): MetadataClass {
    val clazz = MetadataClassBuilder()
    clazz.init()
    return clazz.build()
}

fun metadataList(containsType: MetadataType = PrimitiveNull) = MetadataList(containsType)