package abm.core.data.model

import abm.core.data.model.metadata.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

/**
 * @author: andrei shlykov
 * @since: 20.01.2018
 */
class DataModelTest {

    companion object {
        @JvmStatic
        fun generalizeTestArguments(): Stream<Arguments> = Stream.of(
                Arguments.of("{}", "{}", MetadataClass()),
                Arguments.of("{ \"p0\": 42 }", "{}", metadataClass { + PrimitiveDouble }),
                Arguments.of("{ \"p0\": 42 }", "{ \"p0\": 42 }", metadataClass { + PrimitiveDouble }),
                Arguments.of("{ \"a\": 42 }", "{ \"b\": 42 }",
                        metadataClass {
                            + ("a" to PrimitiveDouble)
                            + ("b" to PrimitiveDouble)
                        }
                ),
                Arguments.of("{ \"a\": 42, \"s\": 24 }", "{ \"a\": 42, \"s\": \"some string\" }",
                        metadataClass {
                            + ("s" to PrimitiveString)
                            + ("a" to PrimitiveDouble)
                        }
                )
        )
    }

    // тест объеденения двух моделей
    @ParameterizedTest
    @MethodSource("generalizeTestArguments")
    fun generalizeTest(json0: String, json1: String, expected: Metadata) {
        val model = DataModel()

        model.generalize(json0)
        model.generalize(json1)

        Assertions.assertEquals(expected, model.metadata)
    }


    @TestFactory
    fun extractModelTest() = listOf(
            "{\"p0\": 5}" to metadataClass { +PrimitiveDouble },
            "{\"a\": 5}" to metadataClass { +("a" to PrimitiveDouble) },
            "{\"p0\": [[\"tests\"], [\"amazing\"]]}" to metadataClass { +metadataList(metadataList(PrimitiveString)) }
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("extractModelTest. Data index: $index") {
            val model = DataModel()
            model.generalize(input)
            Assertions.assertEquals(expected, model.metadata)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [
        """{}""",
        """{ "a": "test string" }""",
        """{ "a": "test string", "b": 42 }""",
        """{ "a": "test string", "b": 42, "c": { "d": true } }""",
        """{ "l": [1, 2, 3, 4, 5] }""",

        """[]""",
        """[42]""",
        """["test string"]""",
        """[{ "b": 42 }]""",
        """[{ "b": 42 }, { "b": 42 }]""",
        """[{ "a": "test string" }, { "b": 42 }]""",
        """[[]]""",
        """[[{}]]""",
        """[[{ "b": 42 }]]"""
    ])
    fun generalizeWithSameObjectTest(json: String) {
        val model = DataModel()

        val metadata0 = model.generalize(json)
        val metadata1 = model.generalize(json)

        Assertions.assertEquals(metadata0, PrimitiveNull)
        Assertions.assertEquals(metadata1, model.metadata)
    }

//    @Test
//    fun getPropertyByValidPathTest() {
//        val model = DataModel()
//        model.metadata = metadataClass {
//            + metadataClass {
//                + metadataClass {
//                    + metadataClass {
//                        + PrimitiveLong
//                    }
//                }
//            }
//        }
//
//        Assertions.assertEquals(PrimitiveLong, model.getMetadataByPath("p0.p0.p0.p0"))
//    }
//
//    @Test
//    fun getPropertyByInvalidPathTest() {
//        val model = DataModel()
//        model.metadata = metadataClass {
//            + metadataClass {
//                + metadataClass {
//                    + metadataClass {
//                        + PrimitiveLong
//                    }
//                }
//            }
//        }
//
//        Assertions.assertThrows(RuntimeException::class.java) { model.getMetadataByPath("p0.p0.p0.p1") }
//    }

}