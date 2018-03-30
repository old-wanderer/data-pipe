package datapipe.core.data.model.metadata.parser.visitor

import datapipe.core.data.model.metadata.PrimitiveString
import datapipe.core.data.model.metadata.metadataClass
import datapipe.core.data.model.metadata.parser.buildMetadata
import datapipe.core.data.model.metadata.parser.buildMetadataAstTree
import datapipe.core.data.model.metadata.parser.tokenize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author: Andrei Shlykov
 * @since: 28.03.2018
 */
class AliasForBadNameVisitorTest {

    @TestFactory
    fun defaultParameterTests() = listOf(
            metadataClass { + "_test" to PrimitiveString } to metadataClass { + "_test" to PrimitiveString },
            metadataClass { + "1test" to PrimitiveString } to metadataClass { + "test" or "1test" to PrimitiveString },
            metadataClass { + "_t&est" to PrimitiveString } to metadataClass { + "_test" or "_t&est" to PrimitiveString },
            metadataClass { + "Test" to PrimitiveString } to metadataClass { + "Test" to PrimitiveString },
            metadataClass { + "test\$one" to PrimitiveString } to metadataClass { + "test\$one" to PrimitiveString },
            metadataClass { + "test1" to PrimitiveString } to metadataClass { + "test1" to PrimitiveString }
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("checkJavaIdentifierTests. Data index: $index") {
            val root = buildMetadataAstTree(tokenize(input).toList())
            val visitor = AliasForBadNameVisitor()
            root.levelOrderIterator().forEach { it.visit(visitor) }
            val actual = buildMetadata(root)
            Assertions.assertEquals(expected, actual)
        }
    }

    @TestFactory
    fun customParameterTest() = listOf(
            metadataClass { + "_test" to PrimitiveString } to metadataClass { + "test" or "_test" to PrimitiveString },
            metadataClass { + "___test" to PrimitiveString } to metadataClass { + "test" or "___test" to PrimitiveString },
            metadataClass { + "_t&est" to PrimitiveString } to metadataClass { + "test" or "_t&est" to PrimitiveString },
            metadataClass { + "Test" to PrimitiveString } to metadataClass { + "Test" to PrimitiveString },
            metadataClass { + "123_test\$one" to PrimitiveString } to metadataClass { + "test\$one" or "123_test\$one" to PrimitiveString },
            metadataClass { + "test1" to PrimitiveString } to metadataClass { + "test1" to PrimitiveString }
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("checkJavaIdentifierTests. Data index: $index") {
            val root = buildMetadataAstTree(tokenize(input).toList())

            fun isFirstUnderscore(parameter: String) = parameter[0] == '_'
            fun firstUnderscoreProcessor(parameter: String) = parameter.dropWhile { it == '_' }

            // TODO когда в котлин добавят поддержку sam, то будет веселее (Вроде в 1.3). Пока что так
            // компилятор не может вывести тип параметров лямбды
            // val visitor = AliasForBadNameVisitor(
            //      AliasForBadNameVisitor.defaultPredicate to AliasForBadNameVisitor.defaultProcessor,
            //      { parameter -> AliasForBadNameVisitor.defaultPredicate(parameter) || parameter[0] == '_' } to AliasForBadNameVisitor.defaultProcessor
            // )
            val visitor = AliasForBadNameVisitor(
                    AliasForBadNameVisitor.defaultPredicate to AliasForBadNameVisitor.defaultProcessor,
                    ::isFirstUnderscore to ::firstUnderscoreProcessor
            )
            root.levelOrderIterator().forEach { it.visit(visitor) }
            val actual = buildMetadata(root)
            Assertions.assertEquals(expected, actual)
        }
    }

}