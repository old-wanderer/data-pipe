package abm.core.data.model.metadata

/**
 * @author: andrei shlykov
 * @since: 19.01.2018
 */

abstract class Metadata {

    abstract infix fun combine(other: Metadata): Metadata

    abstract fun prettyString(depth: Int = 0): String

}

abstract class MetadataType: Metadata() {

    infix fun combine(other: MetadataType): MetadataType = (this combine other as Metadata) as MetadataType

}

// ----------------------------------------------------------------------

class MetadataList(val containsType: MetadataType): MetadataType() {

    override fun combine(other: Metadata): MetadataList = when(other) {
        is MetadataList  -> MetadataList(containsType combine other.containsType)
        is MetadataClass -> MetadataList(containsType combine other)
        PrimitiveNull -> this
        else -> throw TypesNotCombineException(this, other)
    }

    override fun equals(other: Any?) = other is MetadataList && containsType == other.containsType

    override fun hashCode() = containsType.hashCode()

    override fun prettyString(depth: Int) = "[${containsType.prettyString(depth+1)}]"

    override fun toString() = prettyString()
}

sealed class MetadataPrimitive: MetadataType() {

    override fun combine(other: Metadata): Metadata = when {
        this == other -> this
        this == PrimitiveString || other == PrimitiveString -> PrimitiveString
        this == PrimitiveDouble || other == PrimitiveDouble -> PrimitiveDouble
        this == PrimitiveNull -> other
        other == PrimitiveNull -> this
        else -> throw TypesNotCombineException(this, other)
    }

    override fun prettyString(depth: Int) = this::class.simpleName!!

    override fun toString() = prettyString()
}

object PrimitiveLong: MetadataPrimitive()
object PrimitiveDouble: MetadataPrimitive()
object PrimitiveBoolean: MetadataPrimitive()
object PrimitiveString: MetadataPrimitive()
object PrimitiveNull: MetadataPrimitive()