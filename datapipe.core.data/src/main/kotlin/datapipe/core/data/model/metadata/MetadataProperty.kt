package datapipe.core.data.model.metadata

/**
 * @author: Andrei Shlykov
 * @since: 18.03.2018
 */
open class MetadataProperty(val name: String,
                            val type: MetadataType,
                            val aliasNames: Set<String> = emptySet()): Metadata() {

    override fun combine(other: Metadata): MetadataProperty = when (other) {
        is MetadataProperty -> MetadataProperty(name, type combine other.type, aliasNames + other.aliasNames)
        else -> throw TypesNotCombineException(type, other)
    }

    override fun prettyString(depth: Int): String {
        val name = if (aliasNames.isEmpty()) name else "$name | ${aliasNames.joinToString(" | ")}"
        return "${"\t".repeat(depth)}$name: ${type.prettyString(depth)}"
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + aliasNames.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetadataProperty

        if (name != other.name) return false
        if (type != other.type) return false
        if (aliasNames != other.aliasNames) return false

        return true
    }

}