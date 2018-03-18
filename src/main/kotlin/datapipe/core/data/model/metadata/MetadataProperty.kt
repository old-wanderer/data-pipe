package datapipe.core.data.model.metadata

/**
 * @author: Andrei Shlykov
 * @since: 18.03.2018
 */
data class MetadataProperty(val name: String,
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
}