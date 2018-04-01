package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.dsl.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 25.02.2018
 */

/**
 * Тесты построения построения/генерации метаданных по ast
 */
class BuildMetadataFormAstTest {

    @Test
    fun case1() {
        val tokens = listOf(ObjectBegin, PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd)
        val expected = metadataClass { +PrimitiveString }

        Assertions.assertEquals(expected, buildMetadata(buildMetadataAstTree(tokens)))
    }

    @Test
    fun case2() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), TypeSeparator, PrimitiveToken(PrimitiveDouble),
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
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), TypeSeparator, ObjectBegin,
                        PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    ObjectEnd,
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
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), TypeSeparator, ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
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
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), TypeSeparator, ListBegin,
                        ObjectBegin, PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd,
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