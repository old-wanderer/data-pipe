package abm.core.data.model.metadata

/**
 * @author: Andrei Shlykov
 * @since: 22.02.2018
 */
sealed class MetadataToken
data class PropertyToken(val prop: PropertyMetadata): MetadataToken()
object ObjectBegin: MetadataToken()
object ObjectEnd: MetadataToken()
object ListBegin: MetadataToken()
object ListEnd: MetadataToken()