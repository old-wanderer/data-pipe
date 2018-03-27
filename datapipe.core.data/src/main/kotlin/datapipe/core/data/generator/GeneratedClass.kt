package datapipe.core.data.generator

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.metadataClass

/**
 * @author: Andrei Shlykov
 * @since: 14.02.2018
 */
abstract class GeneratedClass {


    companion object {
        @JvmField
        val _metadata: MetadataClass = metadataClass {  }
    }

    fun getPropertyValue(propertyNameChain: String): Any? {
        if (propertyNameChain.isNotBlank()) {
            val propertyNames = propertyNameChain.split(".")
            var current: Any? = getValue(propertyNames.first())

            for (name in propertyNames.drop(1)) {
                if (current is GeneratedClass) {
                    current = current.getValue(name)
                }
            }

            return current
        }
        return null
    }

    fun setPropertyValue(propertyNameChain: String, value: Any?) {
        javaClass.getDeclaredField(propertyNameChain).set(this, value)
    }

    private fun getValue(propName: String): Any? {
        val field = javaClass.getDeclaredField(propName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        return field.get(this)
    }

}

fun Class<out GeneratedClass>.metadata(): MetadataClass {
    return this.getField("_metadata").get(null) as MetadataClass
}