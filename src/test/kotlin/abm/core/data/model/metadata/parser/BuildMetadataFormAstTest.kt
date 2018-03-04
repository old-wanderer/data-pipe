package abm.core.data.model.metadata.parser

import abm.core.data.model.metadata.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 25.02.2018
 */
class BuildMetadataFormAstTest {

    @Test
    fun case1() {
        val tokens = listOf(ObjectBegin, PropertyNameToken("p0"), PrimitiveToken(PrimitiveString), ObjectEnd)
        val expected = metadataClass { +PrimitiveString }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun case2() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), PrimitiveToken(PrimitiveDouble),
                ObjectEnd)
        val expected = metadataClass {
            +PrimitiveString
            +PrimitiveDouble
        }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun case3() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), ObjectBegin, PropertyNameToken("p0"), PrimitiveToken(PrimitiveString), ObjectEnd,
                ObjectEnd)
        val expected = metadataClass {
            +PrimitiveString
            +metadataClass {
                +PrimitiveString
            }
        }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun case4() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
                ObjectEnd)
        val expected = metadataClass {
            +PrimitiveString
            +metadataList(PrimitiveString)
        }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun case5() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), ListBegin,
                        ObjectBegin, PropertyNameToken("p0"), PrimitiveToken(PrimitiveString), ObjectEnd,
                    ListEnd,
                ObjectEnd)
        val expected = metadataClass {
            +PrimitiveString
            +metadataList(metadataClass {
                +PrimitiveString
            })
        }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun bigCase1() {
        val model = metadataClass {
            +PrimitiveLong
            +PrimitiveString
            +PrimitiveDouble
            +metadataList(PrimitiveString)
            +metadataList(PrimitiveBoolean)
            +metadataClass {
                +PrimitiveString
                +PrimitiveString
            }
            +metadataClass {
                +PrimitiveBoolean
                +PrimitiveDouble
                +metadataClass {
                    +PrimitiveString
                    +PrimitiveString
                }
            }
            +PrimitiveString
            +metadataList(metadataClass {
                +PrimitiveString
                +PrimitiveString
            })
        }

        Assertions.assertEquals(model, buildMetadata(buildMetadataAstTree(tokenize(model).toList())))
    }

}