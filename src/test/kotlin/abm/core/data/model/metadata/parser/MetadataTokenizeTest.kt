package abm.core.data.model.metadata.parser

import abm.core.data.model.metadata.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class MetadataTokenizeTest {

    @Test
    fun allPropsVisit() {
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p1 = PropertyMetadata("p1", PrimitiveString)
        val p2 = PropertyMetadata("p2", PrimitiveBoolean)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataClass(setOf(p4, p5)))
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
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p1 = PropertyMetadata("p1", PrimitiveString)
        val p2 = PropertyMetadata("p2", PrimitiveBoolean)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataClass(setOf(p4, p5)))
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
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataList(MetadataClass(setOf(p4, p5))))
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
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p3 = PropertyMetadata("p3", MetadataList(PrimitiveString))
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

}