package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.PrimitiveString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 24.02.2018
 */

/**
 * Тесты построения ast по списку токенов [MetadataToken]
 */
class MetadataAstBuildTest {

    @Test
    fun emptyObjectTest() {
        val tokens = listOf(ObjectBegin, ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        Assertions.assertTrue(ast is RootNode)
        Assertions.assertTrue((ast as RootNode).child is MetadataClassNode)
    }

    @Test
    fun onePropertyPrimitiveTest() {
        val tokens = listOf(ObjectBegin, PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        var current = ast
        Assertions.assertTrue(current is RootNode)
        Assertions.assertTrue((current as RootNode).child is MetadataClassNode)

        current = current.child as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataPrimitiveNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        Assertions.assertTrue(current.type is MetadataPrimitiveNode)
        current = current.type as MetadataPrimitiveNode
        Assertions.assertEquals(PrimitiveString, current.type)
    }

    @Test
    fun twoPropertyPrimitiveTest() {
        val tokens = listOf(
                ObjectBegin,
                    PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString),
                    PropertyNameToken("p1"), TypeSeparator, PrimitiveToken(PrimitiveString),
                ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        var current = ast
        Assertions.assertTrue(current is RootNode)
        Assertions.assertTrue((current as RootNode).child is MetadataClassNode)

        current = current.child as MetadataClassNode
        Assertions.assertEquals(2, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataPrimitiveNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        Assertions.assertTrue(current.type is MetadataPrimitiveNode)
        current = current.type as MetadataPrimitiveNode
        Assertions.assertEquals(PrimitiveString, current.type)
    }

    @Test
    fun onePropertyObjectTest() {
        val tokens = listOf(
                ObjectBegin, PropertyNameToken("p0"), TypeSeparator,
                    ObjectBegin, PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd,
                ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        var current = ast
        Assertions.assertTrue(current is RootNode)
        Assertions.assertTrue((current as RootNode).child is MetadataClassNode)

        current = current.child as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataClassNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        current = current.type as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataPrimitiveNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        Assertions.assertTrue(current.type is MetadataPrimitiveNode)
        current = current.type as MetadataPrimitiveNode
        Assertions.assertEquals(PrimitiveString, current.type)
    }

    @Test
    fun onePropertyListOfPrimitiveTest() {
        val tokens = listOf(
                ObjectBegin, PropertyNameToken("p0"), TypeSeparator,
                    ListBegin, PrimitiveToken(PrimitiveString), ListEnd,
                ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        var current = ast
        Assertions.assertTrue(current is RootNode)
        Assertions.assertTrue((current as RootNode).child is MetadataClassNode)

        current = current.child as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataListNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        current = current.type as MetadataListNode
        Assertions.assertTrue(current.containedType is MetadataPrimitiveNode)
        current = current.containedType as MetadataPrimitiveNode
        Assertions.assertTrue(current.type is PrimitiveString)
    }

    @Test
    fun onePropertyListOfObjectTest() {
        val tokens = listOf(
                ObjectBegin, PropertyNameToken("p0"), TypeSeparator,
                    ListBegin,
                        ObjectBegin, PropertyNameToken("p0"), TypeSeparator, PrimitiveToken(PrimitiveString), ObjectEnd,
                    ListEnd,
                ObjectEnd)
        val ast = buildMetadataAstTree(tokens)

        var current = ast
        Assertions.assertTrue(current is RootNode)
        Assertions.assertTrue((current as RootNode).child is MetadataClassNode)

        current = current.child as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        Assertions.assertTrue(current.properties.first().type is MetadataListNode)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        current = current.type as MetadataListNode
        Assertions.assertTrue(current.containedType is MetadataClassNode)
        current = current.containedType as MetadataClassNode
        Assertions.assertEquals(1, current.properties.size)
        current = current.properties.first()
        Assertions.assertEquals("p0", current.names.first().name)
        Assertions.assertTrue(current.type is MetadataPrimitiveNode)
        current = current.type as MetadataPrimitiveNode
        Assertions.assertEquals(PrimitiveString, current.type)

    }


    // TODO перенести тест
    // TODO тест может легко сломаться, хотя вариант прохода будет правильный
    //      надо проверять, что узел посещается, если все дети были посещены ранее
    @Test
    fun postOrderTreeIteratorTest() {
        val root = RootNode()
        val child1 = MetadataClassNode(root)
        val child1_1 = MetadataPropertyNode(child1)
        val child1_2 = MetadataPropertyNode(child1)
        val child1_3 = MetadataPropertyNode(child1)

        val child1_1_1 = MetadataPropertyNameNode("child1_1_1", child1_1)
        val child1_1_2 = MetadataPrimitiveNode(PrimitiveString, child1_1)

        val child1_2_1 = MetadataPropertyNameNode("child1_2_1", child1_2)
        val child1_2_2 = MetadataListNode(child1_2)
        val child1_2_2_1 = MetadataPrimitiveNode(PrimitiveString, child1_2_2)

        val child1_3_1 = MetadataPropertyNameNode("child1_3_1", child1_3)
        val child1_3_2 = MetadataClassNode(child1_3)
        val child1_3_2_1 = MetadataPropertyNode(child1_3_2)
        val child1_3_2_1_1 = MetadataPropertyNameNode("child1_3_2_1_1", child1_3_2_1)
        val child1_3_2_1_2 = MetadataPrimitiveNode(PrimitiveString, child1_3_2_1)

        val lst = listOf(
                child1_1_1, child1_1_2, child1_1,
                child1_2_1, child1_2_2_1, child1_2_2, child1_2,
                child1_3_1, child1_3_2_1_1, child1_3_2_1_2, child1_3_2_1, child1_3_2, child1_3,
                child1, root
        )

        for ((index, node) in  root.postOrderIterator().withIndex()) {
            Assertions.assertEquals(lst[index], node, "index: $index")
        }

    }
}