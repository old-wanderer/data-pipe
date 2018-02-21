package abm.core.data.model.metadata

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class PropertiesMetadataClassIteratorTest {

    @Test
    fun allPropsVisit() {
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p1 = PropertyMetadata("p1", PrimitiveString)
        val p2 = PropertyMetadata("p2", PrimitiveBoolean)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataClass(setOf(p4, p5)))
        val props = mutableSetOf(p0, p1, p2, p3, p4, p5)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for (property in model.propertyIterator()) {
            Assertions.assertTrue(property in props)
            props.remove(property)
        }
        Assertions.assertTrue(props.isEmpty())
    }

    @Test
    fun preorderTest() {
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p1 = PropertyMetadata("p1", PrimitiveString)
        val p2 = PropertyMetadata("p2", PrimitiveBoolean)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataClass(setOf(p4, p5)))
        val props = mutableListOf(p0, p1, p2, p3, p4, p5)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for ((index, property) in model.propertyIterator().withIndex()) {
            Assertions.assertEquals(props[index], property)
        }
    }

}