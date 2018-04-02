package datapipe.core.data.generator

import datapipe.core.data.model.metadata.MetadataClass
import datapipe.core.data.model.metadata.dsl.metadataClass

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
        if (propertyNameChain.isNotBlank()) {
            val propertyNames = propertyNameChain.split(".")
            var current: Any? = this

            for (name in propertyNames.dropLast(1)) {
                if (current is GeneratedClass) {
                    val next = current.getValue(name)
                    current = if (next == null) {
                        val cls = current.getField(name).type
                        if (GeneratedClass::class.java.isAssignableFrom(cls)) {
                            current.getField(name).set(current, cls.getConstructor().newInstance())
                            current.getValue(name)
                        } else {
                            throw RuntimeException("can't find field or assign value")
                        }
                    } else {
                        next
                    }
                }
            }

            if (current is GeneratedClass) {
                current.setValue(propertyNames.last(), value)
            }
        }
    }

    private fun getField(name: String) = javaClass.getDeclaredField(name)
    private fun getValue(name: String) = getField(name).get(this)
    private fun setValue(name: String, value: Any?) = getField(name).set(this, value)

}

fun Class<out GeneratedClass>.metadata(): MetadataClass {
    return this.getField("_metadata").get(null) as MetadataClass
}