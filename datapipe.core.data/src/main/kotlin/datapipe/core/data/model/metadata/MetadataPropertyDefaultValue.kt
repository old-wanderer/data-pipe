package datapipe.core.data.model.metadata

/**
 * @author: Andrei Shlykov
 * @since: 03.04.2018
 */
class MetadataPropertyDefaultValue(name: String,
                                   type: MetadataType,
                                   val defaultValue: Any, // TODO wrapper MetadataPrimitiveValue
                                   aliases: Set<String> = emptySet())
    : MetadataProperty(name, type, aliases)
{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as MetadataPropertyDefaultValue

        if (defaultValue != other.defaultValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + defaultValue.hashCode()
        return result
    }
}