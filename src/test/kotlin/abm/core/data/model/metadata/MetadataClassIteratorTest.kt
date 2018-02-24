package abm.core.data.model.metadata

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class MetadataClassIteratorTest {

    @Test
    fun allPropsVisit() {
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p1 = PropertyMetadata("p1", PrimitiveString)
        val p2 = PropertyMetadata("p2", PrimitiveBoolean)
        val p4 = PropertyMetadata("p4", PrimitiveString)
        val p5 = PropertyMetadata("p5", PrimitiveDouble)
        val p3 = PropertyMetadata("p3", MetadataClass(setOf(p4, p5)))
        val expectedTokens = mutableListOf(
                ObjectBegin, PropertyToken(p0), PropertyToken(p1), PropertyToken(p2), PropertyToken(p3),
                ObjectBegin, PropertyToken(p4), PropertyToken(p5), ObjectEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for (token in model.propertyIterator()) {
            Assertions.assertTrue(token in expectedTokens) { "token $token not contains" }
            expectedTokens.removeAt(expectedTokens.indexOf(token))
        }
        Assertions.assertTrue(expectedTokens.isEmpty())
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
                ObjectBegin, PropertyToken(p0), PropertyToken(p1), PropertyToken(p2), PropertyToken(p3),
                ObjectBegin, PropertyToken(p4), PropertyToken(p5), ObjectEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p1, p2, p3))

        for ((index, token) in model.propertyIterator().withIndex()) {
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

                ObjectBegin, PropertyToken(p0), PropertyToken(p3),
                    ListBegin,
                        ObjectBegin, PropertyToken(p4), PropertyToken(p5), ObjectEnd,
                    ListEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p3))

        for ((index, token) in model.propertyIterator().withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

    @Test
    fun withListOfPrimitiveTest() {
        val p0 = PropertyMetadata("p0", PrimitiveLong)
        val p3 = PropertyMetadata("p3", MetadataList(PrimitiveString))
        val expectedTokens = mutableListOf(
                ObjectBegin, PropertyToken(p0), PropertyToken(p3),
                ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
                ObjectEnd)
        val model = MetadataClass(setOf(p0, p3))

        for ((index, token) in model.propertyIterator().withIndex()) {
            Assertions.assertEquals(expectedTokens[index], token) { "index: $index" }
        }
    }

}

//    @Test
//    fun reconstructMetadataClassByTokesTest() {
//
//        fun Iterator<MetadataToken>.reconstructMetadataClass(): MetadataClass {
//
//            val stack = LinkedList<Metadata>()
//
//            for (token in this) {
//                when (token) {
//                    ObjectBegin -> stack.push(MetadataClass())
//                    is PropertyToken -> stack.peek() combine MetadataClass(setOf(token.prop))
//                    ObjectEnd   -> {
//                        if (stack.size > 1) {
//                            val current = stack.poll()
//                            val meta = stack.peek()
//                            when(meta) {
//                                is MetadataClass -> meta.properties.last().type combine current
//                                is MetadataList  -> meta.containsType combine current
//                            }
//                        }
//                    }
//                    ListBegin -> stack.push(mutableListOf())
//                    ListEnd -> {
//                        val props = stack.poll()
//                         props.size Э [0..1]
//                        val meta = stack.peek()
//                        val meta_prop = meta.last()
//                        meta[meta.lastIndex] = PropertyMetadata(meta_prop.name, MetadataList(props.first()), meta_prop.aliasNames)
//                    }
//                }
//            }
//
//            return MetadataClass(stack.first().toSet())
//        }
//
//        val metadata = metadataClass {
//            + PrimitiveBoolean
//            + ("second" to PrimitiveBoolean)
//            + ("third" to PrimitiveBoolean)
//            + metadataClass {
//                + PrimitiveBoolean
//                + PrimitiveBoolean
//                + PrimitiveBoolean
//            }
//            + metadataList(metadataClass {
//                + PrimitiveString
//            })
//        }
//
//        Assertions.assertEquals(metadata, metadata.propertyIterator().reconstructMetadataClass())
//
//    }

//}