package datapipe.core.data.model.metadata.dsl

import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.dsl.builder.MetadataClassBuilder

/**
 * @author: Andrei Shlykov
 * @since: 31.03.2018
 */
fun metadataClass(init: MetadataClassBuilder.() -> Unit): MetadataClass {
    val clazz = MetadataClassBuilder()
    clazz.init()
    return clazz.build()
}

fun metadataList(containsType: MetadataType = PrimitiveNull) = MetadataList(containsType)