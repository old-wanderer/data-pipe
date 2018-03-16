package datapipe.core.data.generator

import datapipe.core.data.model.metadata.MetadataClass
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * @author: Andrei Shlykov
 * @since: 16.03.2018
 */
object GeneratedClassesCache: ReadOnlyProperty<MetadataClass, Class<GeneratedClass>> {

    private val cache = mutableMapOf<MetadataClass, Class<GeneratedClass>>()

    operator fun get(metadataClass: MetadataClass): Class<GeneratedClass> =
        cache.getOrPut(metadataClass) { ClassGenerator.generateClass(metadataClass) }

    override fun getValue(thisRef: MetadataClass, property: KProperty<*>): Class<GeneratedClass> = get(thisRef)

}
