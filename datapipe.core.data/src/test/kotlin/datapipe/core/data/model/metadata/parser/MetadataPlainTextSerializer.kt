package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.PrimitiveString
import datapipe.core.data.model.metadata.metadataClass
import datapipe.core.data.model.metadata.metadataList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author: Andrei Shlykov
 * @since: 07.03.2018
 */
class MetadataPlainTextSerializer {

    @TestFactory
    fun plainTextSerializerTest() = listOf(
            metadataClass {  } to "{} ",
            metadataClass { +PrimitiveString } to "{p0:PrimitiveString } ",
            metadataClass {
                +PrimitiveString
                +PrimitiveString
            } to "{p0:PrimitiveString p1:PrimitiveString } ",
            metadataClass {
                +PrimitiveString
                + "p1" or "p2" or "p3" to PrimitiveString
            } to "{p0:PrimitiveString p1|p2|p3:PrimitiveString } ",
            metadataClass {
                +PrimitiveString
                + metadataList(PrimitiveString)
            } to "{p0:PrimitiveString p1:[PrimitiveString ] } "
    ).mapIndexed { index, (input, expected) ->
        DynamicTest.dynamicTest("tokenizeTests. Data index: $index") {
            Assertions.assertEquals(expected, serialize(input))
        }
    }

}