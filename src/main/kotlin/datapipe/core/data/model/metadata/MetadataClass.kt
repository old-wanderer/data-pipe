package datapipe.core.data.model.metadata

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.generator.GeneratedClassesCache

/**
 * @author: Andrei Shlykov
 * @since: 31.01.2018
 */
class MetadataClass(val properties: Set<PropertyMetadata> = emptySet()): MetadataType() {

    val generatedClass: Class<GeneratedClass> by GeneratedClassesCache

    override fun combine(other: Metadata): Metadata = when(other) {
        is MetadataClass -> {
            val properties = HashMap<String, PropertyMetadata>(this.properties.size + other.properties.size)
            properties.putAll(this.properties.map { Pair(it.name, it) })

            for (property in other.properties) {
                properties.compute(property.name) { _, v -> v?.combine(property) ?: property }
            }

            MetadataClass(properties.values.toSet())
        }
        is MetadataList -> MetadataList(this combine other.containsType)
        PrimitiveNull -> this
        else -> throw TypesNotCombineException(this, other)
    }

    override fun prettyString(depth: Int): String {
        val prefix = "\t".repeat(depth)
        val maxLength = (properties.maxBy { it.name.length }?.name?.length ?: 0) + 1

        return buildString {
            append("{\n")
            properties.sortedBy(PropertyMetadata::name).forEach {
                val aliases = if (it.aliasNames.isEmpty()) "" else it.aliasNames.joinToString(" | ", prefix=" (", postfix=")")
                append("${"\t".repeat(depth+1)}%-${maxLength}s: %s%s\n"
                        .format(it.name, it.type.prettyString(depth+1), aliases))
            }
            append("$prefix}")
        }
    }

    override fun equals(other: Any?) = other is MetadataClass && properties == other.properties

    override fun hashCode() = properties.hashCode()

    override fun toString() = prettyString()
}

data class PropertyMetadata(val name: String,
                            val type: MetadataType,
                            val aliasNames: Set<String> = emptySet()): Metadata() {

    override fun combine(other: Metadata): PropertyMetadata = when (other) {
        is PropertyMetadata -> PropertyMetadata(name, type combine other.type, aliasNames + other.aliasNames)
        else -> throw TypesNotCombineException(type, other)
    }

    override fun prettyString(depth: Int): String {
        val name = if (aliasNames.isEmpty()) name else "$name | ${aliasNames.joinToString(" | ")}"
        return "${"\t".repeat(depth)}$name: ${type.prettyString(depth)}"
    }
}