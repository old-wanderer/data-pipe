package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.dsl.metadataClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class MetadataTokenizeTest {

    @Test
    fun allPropsVisit() {
        val p0 = MetadataProperty("p0", PrimitiveLong)
        val p1 = MetadataProperty("p1", PrimitiveString)
        val p2 = MetadataProperty("p2", PrimitiveBoolean)
        val p4 = MetadataProperty("p4", PrimitiveString)
        val p5 = MetadataProperty("p5", PrimitiveDouble)
        val p3 = MetadataProperty("p3", MetadataClass(setOf(p4, p5)))
        val expectedTokens = mutableListOf(
                ObjectBegin,
                    PropertyNameToken(p0.name), TypeSeparator, PrimitiveToken(p0.type as MetadataPrimitive),
                    PropertyNameToken(p1.name), TypeSeparator, PrimitiveToken(p1.type as MetadataPrimitive),
                    PropertyNameToken(p2.name), TypeSeparator, PrimitiveToken(p2.type as MetadataPrimitive),
                    PropertyNameToken(p3.name), TypeSeparator, ObjectBegin,
                        PropertyNameToken(p4.name), TypeSeparator, PrimitiveToken(p4.type as MetadataPrimitive),
                        PropertyNameToken(p5.name), TypeSeparator, PrimitiveToken(p5.type as MetadataPrimitive),
                    ObjectEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for (token in tokenize(model)) {
            Assertions.assertTrue(token in expectedTokens) { "token $token not contains" }
            expectedTokens.removeAt(expectedTokens.indexOf(token))
        }
        Assertions.assertTrue(expectedTokens.isEmpty()) { "remained: $expectedTokens" }
    }

    @Test
    fun preorderTest() {
        val p0 = MetadataProperty("p0", PrimitiveLong)
        val p1 = MetadataProperty("p1", PrimitiveString)
        val p2 = MetadataProperty("p2", PrimitiveBoolean)
        val p4 = MetadataProperty("p4", PrimitiveString)
        val p5 = MetadataProperty("p5", PrimitiveDouble)
        val p3 = MetadataProperty("p3", MetadataClass(setOf(p4, p5)))
        val expectedTokens = mutableListOf(
                ObjectBegin,
                    PropertyNameToken(p0.name), TypeSeparator, PrimitiveToken(p0.type as MetadataPrimitive),
                    PropertyNameToken(p1.name), TypeSeparator, PrimitiveToken(p1.type as MetadataPrimitive),
                    PropertyNameToken(p2.name), TypeSeparator, PrimitiveToken(p2.type as MetadataPrimitive),
                    PropertyNameToken(p3.name), TypeSeparator, ObjectBegin,
                        PropertyNameToken(p4.name), TypeSeparator, PrimitiveToken(p4.type as MetadataPrimitive),
                        PropertyNameToken(p5.name), TypeSeparator, PrimitiveToken(p5.type as MetadataPrimitive),
                    ObjectEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for ((index, token) in tokenize(model).withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

    @Test
    fun withListTest() {
        val p0 = MetadataProperty("p0", PrimitiveLong)
        val p4 = MetadataProperty("p4", PrimitiveString)
        val p5 = MetadataProperty("p5", PrimitiveDouble)
        val p3 = MetadataProperty("p3", MetadataList(MetadataClass(setOf(p4, p5))))
        val expectedTokens = mutableListOf(
                ObjectBegin,
                    PropertyNameToken(p0.name), TypeSeparator, PrimitiveToken(PrimitiveLong),
                    PropertyNameToken(p3.name), TypeSeparator,
                        ListBegin,
                            ObjectBegin,
                                PropertyNameToken(p4.name), TypeSeparator, PrimitiveToken(p4.type as MetadataPrimitive),
                                PropertyNameToken(p5.name), TypeSeparator, PrimitiveToken(p5.type as MetadataPrimitive),
                            ObjectEnd,
                        ListEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p3))

        for ((index, token) in tokenize(model).withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

    @Test
    fun withListOfPrimitiveTest() {
        val p0 = MetadataProperty("p0", PrimitiveLong)
        val p3 = MetadataProperty("p3", MetadataList(PrimitiveString))
        val expectedTokens = mutableListOf(
                ObjectBegin,
                    PropertyNameToken(p0.name), TypeSeparator, PrimitiveToken(PrimitiveLong),
                    PropertyNameToken(p3.name), TypeSeparator, ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p3))

        for ((index, token) in tokenize(model).withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

    @Test
    fun withListOfListTest() {
        val p3 = MetadataProperty("p3", MetadataList(MetadataList(PrimitiveString)))
        val expectedTokens = mutableListOf(
                ObjectBegin,
                    PropertyNameToken(p3.name), TypeSeparator, ListBegin,
                        ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
                    ListEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p3))

        for ((index, token) in tokenize(model).withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

    @Test
    fun aliasNameTest() {
        val source = metadataClass {
            + "p0" to PrimitiveLong
            + "p1" or "p01" to PrimitiveLong
            + "p2" or "p02" or "p10" to PrimitiveLong
        }
        val expectedTokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveLong),
                    PropertyNameToken("p1"), AliasSeparator, PropertyNameToken("p01"), TypeSeparator, PrimitiveToken(PrimitiveLong),
                    PropertyNameToken("p2"), AliasSeparator, PropertyNameToken("p02"), AliasSeparator, PropertyNameToken("p10"), TypeSeparator, PrimitiveToken(PrimitiveLong),
                ObjectEnd)
        Assertions.assertEquals(expectedTokens, tokenize(source).toList())
    }

}