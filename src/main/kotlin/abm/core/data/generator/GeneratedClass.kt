package abm.core.data.generator

/**
 * @author: Andrei Shlykov
 * @since: 14.02.2018
 */
abstract class GeneratedClass {

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

    private fun getValue(propName: String): Any? {
        val field = javaClass.getDeclaredField(propName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        return field.get(this)
    }

}