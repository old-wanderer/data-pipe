package datapipe.core.data.generator

import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.dsl.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.function.Executable
import java.lang.reflect.Field

/**
 * @author: Andrei Shlykov
 * @since: 15.02.2018
 */
class GeneratedClassTest {

    private val metadata = metadataClass { 
        + "prop1" to PrimitiveLong
        + "prop2" to PrimitiveString
        + "prop3" to metadataClass { 
            + "prop3_1" to PrimitiveLong
            + "prop3_2" to PrimitiveString
        }
    }
    
    private lateinit var testObject: GeneratedClass
    
    @BeforeEach
    fun initTestObject() {
        testObject = metadata.generatedClass.getConstructor().newInstance()
    }
    

    @Test
    @Disabled // TODO исправить после DP-50
    fun getPropertyValueTest() {
//        Assertions.assertEquals(testObject.prop1, testObject.getPropertyValue("prop1"))
//        Assertions.assertEquals(testObject.prop2, testObject.getPropertyValue("prop2"))
        Assertions.assertEquals(31, testObject.getPropertyValue("prop3.prop3_1"))
        Assertions.assertEquals("prop3_2", testObject.getPropertyValue("prop3.prop3_2"))
    }

    @TestFactory
    fun setPropertyValueTest() = listOf(
            "prop1" to 42L,
            "prop2" to "Test",
            "prop3.prop3_1" to 13L,
            "prop3.prop3_2" to "NestedPropTest"
    ).mapIndexed { index, (path, value) ->
        DynamicTest.dynamicTest("setPropertyValueTest. Data index: $index") {
            testObject.setPropertyValue(path, value)
            Assertions.assertEquals(value, testObject.getPropertyValue(path))
        }
    }

    // TODO перенести в ClassGeneratorTest
    // ABM-6
    @Test
    fun classGeneratedTest() {
        val meta = metadataClass {
            + "primS" to PrimitiveString
            + "primD" to PrimitiveDouble
            + "primB" to PrimitiveBoolean
            + "listPrim" to metadataList(PrimitiveLong)
            + "listObj" to metadataList(metadataClass {
                + "str1" to PrimitiveString
                + "str2" to PrimitiveString
            })
            + "obj" to metadataClass {
                + "primS" to PrimitiveString
                + "primD" to PrimitiveDouble
                + "primB" to PrimitiveBoolean
            }
        }
        val clazz = ClassGenerator.generateClass(meta)
        val fields = clazz.declaredFields

        fun Array<Field>.propertyCheck(name: String, type: Class<*>): Executable = Executable {
            val field = this.find { it.name == name }
            Assertions.assertNotNull(field)
            Assertions.assertEquals(type, field!!.type)
        }

        // TODO ABM-13
        Assertions.assertAll(
                fields.propertyCheck("primS", String::class.java),
                fields.propertyCheck("primD", Double::class.java),
                fields.propertyCheck("primB", Boolean::class.java),
                fields.propertyCheck("listPrim", List::class.java), // TODO generic type check
                fields.propertyCheck("listObj", List::class.java),
                Executable {
                    val nestedObject = clazz.getDeclaredField("obj").type
                    Assertions.assertTrue(GeneratedClass::class.java.isAssignableFrom(nestedObject))
                    val nestedObjectFields = nestedObject.declaredFields
                    Assertions.assertAll(
                            nestedObjectFields.propertyCheck("primS", String::class.java),
                            nestedObjectFields.propertyCheck("primD", Double::class.java),
                            nestedObjectFields.propertyCheck("primB", Boolean::class.java)
                    )
                }
        )

    }

    // TODO перенести в ClassGeneratorTest
    @Test
    fun staticMetadataLinkTest() {
        val metadata = metadataClass {
            + PrimitiveLong
            + PrimitiveString
            + metadataList(PrimitiveDouble)
            + metadataClass {
                + PrimitiveLong
                + PrimitiveString
                + metadataList(PrimitiveDouble)
            }
        }

        Assertions.assertEquals(metadata, ClassGenerator.generateClass(metadata).metadata())
    }


}