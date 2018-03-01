package abm.core.data.model.metadata

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author: Andrei Shlykov
 * @since: 03.03.2018
 */
// TODO add more tests
class MetadataLexerTest {

    private val testGroup1Tokens = listOf(ObjectBegin, PropertyNameToken("name1"), TypeSeparator, PrimitiveToken(PrimitiveString),  ObjectEnd, EOFToken)
    private val testGroup2Tokens = listOf(ObjectBegin, PropertyNameToken("name1"), TypeSeparator, ListBegin, PrimitiveToken(PrimitiveString), ListEnd, ObjectEnd, EOFToken)

    @TestFactory
    fun tokenizeTests() = listOf(
            // region first test group. Object with single primitive property
            "{name1:PrimitiveString}"         to testGroup1Tokens,
            "{name1 :PrimitiveString}"        to testGroup1Tokens,
            "{name1: PrimitiveString}"        to testGroup1Tokens,
            "{name1 : PrimitiveString}"       to testGroup1Tokens,
            "{ name1 : PrimitiveString }"     to testGroup1Tokens,
            "{\nname1 : PrimitiveString\n}"   to testGroup1Tokens,
            "{\n\tname1 : PrimitiveString\n}" to testGroup1Tokens,
            // endregion

            // region second test group. Object with single property of list
            "{name1:[PrimitiveString]}"         to testGroup2Tokens,
            "{name1:[ PrimitiveString]}"        to testGroup2Tokens,
            "{name1:[PrimitiveString ]}"        to testGroup2Tokens,
            "{name1:[ PrimitiveString ]}"       to testGroup2Tokens,
            "{name1:[\n\t\tPrimitiveString\n]}" to testGroup2Tokens
            // endregion
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("tokenizeTests. Data index: $index") {
            Assertions.assertEquals(expected, tokenize(input))
        }
    }

}