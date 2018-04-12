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
        + "prop1" to PrimitiveLong default 11L
        + "prop2" to PrimitiveString default "prop2"
        + "prop3" to metadataClass { 
            + "prop3_1" to PrimitiveLong default 31L
            + "prop3_2" to PrimitiveString default "prop3_2"
        }
    }
    
    private lateinit var testObject: GeneratedClass
    
    @BeforeEach
    fun initTestObject() {
        testObject = metadata.generatedClass.getConstructor().newInstance()
    }

    /**
     * Проверка установки значений по умолчанию и матода [GeneratedClass.getPropertyValue]
     */
    @TestFactory
    fun getPropertyValueTest() = listOf(
            "prop1" to 11L,
            "prop2" to "prop2",
            "prop3.prop3_1" to 31L,
            "prop3.prop3_2" to "prop3_2"
    ).mapIndexed { index, (path, value) ->
        DynamicTest.dynamicTest("getPropertyValueTest. Data index: $index") {
            Assertions.assertEquals(value, testObject.getPropertyValue(path))
        }
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

    @Test
    fun toStringTest() {
        Assertions.assertEquals("", testObject.toString())
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