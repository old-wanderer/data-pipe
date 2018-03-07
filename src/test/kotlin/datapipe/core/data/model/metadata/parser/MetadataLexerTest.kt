package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.PrimitiveLong
import datapipe.core.data.model.metadata.PrimitiveString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author: Andrei Shlykov
 * @since: 03.03.2018
 */
class MetadataLexerTest {

    private val testGroup1Tokens = listOf(ObjectBegin, PropertyNameToken("name1"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd, EOFToken)
    private val testGroup2Tokens = listOf(ObjectBegin, PropertyNameToken("name1"), TypeSeparator, ListBegin, PrimitiveToken(PrimitiveString), ListEnd, ObjectEnd, EOFToken)
    private val testGroup3Tokens = listOf(
            ObjectBegin,
                PropertyNameToken("name1"), TypeSeparator, PrimitiveToken(PrimitiveString),
                PropertyNameToken("name2"), TypeSeparator, PrimitiveToken(PrimitiveLong),
            ObjectEnd, EOFToken)
    private val testGroup4Tokens = listOf(
            ObjectBegin,
                PropertyNameToken("name1"), TypeSeparator, ObjectBegin,
                    PropertyNameToken("name1_1"), TypeSeparator, PrimitiveToken(PrimitiveString),
                ObjectEnd,
            ObjectEnd, EOFToken)
    private val testGroup5Tokens = listOf(
            ObjectBegin,
                PropertyNameToken("name1"),
                    AliasSeparator, PropertyNameToken("alias1"),
                    AliasSeparator, PropertyNameToken("alias2"),
                    TypeSeparator, PrimitiveToken(PrimitiveString),
            ObjectEnd, EOFToken)

    @TestFactory
    fun tokenizeTests() = listOf(
            // region testGroup1Tokens. Object with single primitive property
            "{name1:PrimitiveString}"         to testGroup1Tokens,
            "{name1 :PrimitiveString}"        to testGroup1Tokens,
            "{name1: PrimitiveString}"        to testGroup1Tokens,
            "{name1 : PrimitiveString}"       to testGroup1Tokens,
            "{ name1 : PrimitiveString }"     to testGroup1Tokens,
            "{\nname1 : PrimitiveString\n}"   to testGroup1Tokens,
            "{\n\tname1 : PrimitiveString\n}" to testGroup1Tokens,
            // endregion

            // region testGroup2Tokens. Object with single property of list
            "{name1:[PrimitiveString]}"         to testGroup2Tokens,
            "{name1:[ PrimitiveString]}"        to testGroup2Tokens,
            "{name1:[PrimitiveString ]}"        to testGroup2Tokens,
            "{name1:[ PrimitiveString ]}"       to testGroup2Tokens,
            "{name1:[\n\t\tPrimitiveString\n]}" to testGroup2Tokens,
            // endregion

            // region testGroup3Tokens. Object with not single primitive property
            "{name1:PrimitiveString name2:PrimitiveLong}"        to testGroup3Tokens,
            "{name1:PrimitiveString\tname2:PrimitiveLong}"       to testGroup3Tokens,
            "{\n\tname1:PrimitiveString\n\tname2:PrimitiveLong}" to testGroup3Tokens,
            // endregion

            // region testGroup3Tokens. Object with nested object
            "{name1:{name1_1: PrimitiveString}}"               to testGroup4Tokens,
            "{\nname1:{\n\t\tname1_1: PrimitiveString\n\t}\n}" to testGroup4Tokens,
            // endregion

            // region testGroup3Tokens. Object with nested object
            "{name1|alias1|alias2:PrimitiveString}"        to testGroup5Tokens,
            "{name1 | alias1 | alias2 : PrimitiveString}"  to testGroup5Tokens
            // endregion
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("tokenizeTests. Data index: $index") {
            Assertions.assertEquals(expected, tokenize(input))
        }
    }

}