package datapipe.core.data.model.metadata

import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

/**
 * @author: Andrei Shlykov
 * @since: 27.02.2018
 */
class ConstructMetadataFromJsonTest {

    private val parser = JsonParser()

    companion object {
        @JvmStatic
        fun generalizeTestArguments(): Stream<Arguments> = Stream.of(
                Arguments.of("{}", "{}", MetadataClass()),
                Arguments.of("{ \"p0\": 42 }", "{}", metadataClass { +PrimitiveDouble }),
                Arguments.of("{ \"p0\": 42 }", "{ \"p0\": 42 }", metadataClass { +PrimitiveDouble }),
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
    fun combineTest(json0: String, json1: String, expected: Metadata) {

        val jsonElem0 = parser.parse(json0)
        val jsonElem1 = parser.parse(json1)

        val metadata0 = constructMetadataFromJson(jsonElem0)
        val metadata1 = constructMetadataFromJson(jsonElem1)
        val result = metadata0 combine metadata1

        Assertions.assertEquals(expected, result)
    }


    @TestFactory
    fun extractModelTest() = listOf(
            "{\"p0\": 5}" to metadataClass { +PrimitiveDouble },
            "{\"a\": 5}" to metadataClass { +("a" to PrimitiveDouble) },
            "{\"p0\": [[\"tests\"], [\"amazing\"]]}" to metadataClass { +metadataList(metadataList(PrimitiveString)) }
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("extractModelTest. Data index: $index") {
            val metadata = constructMetadataFromJson(parser.parse(input))
            Assertions.assertEquals(expected, metadata)
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
    fun combineWithSameObjectTest(json: String) {
        val metadata0 = constructMetadataFromJson(parser.parse(json))
        val metadata1 = constructMetadataFromJson(parser.parse(json))

        Assertions.assertEquals(metadata1, metadata0 combine metadata1)
    }

}