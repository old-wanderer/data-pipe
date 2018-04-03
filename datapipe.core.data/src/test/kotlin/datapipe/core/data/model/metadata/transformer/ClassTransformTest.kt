package datapipe.core.data.model.metadata.transformer

import datapipe.core.data.model.metadata.PrimitiveString
import datapipe.core.data.model.metadata.dsl.metadataClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 30.03.2018
 */
class ClassTransformTest {

    @Test
    fun moveOperationTest() {
        val expected = metadataClass {
            + "prop1" to PrimitiveString
            + "prop2" to PrimitiveString
            + "prop3" to PrimitiveString
            + "prop4" to metadataClass {
                + "prop6" to PrimitiveString
                + "prop7" to metadataClass { }
                + "prop9" to PrimitiveString
            }
            + "prop5" to PrimitiveString
            + "prop8" to PrimitiveString
            + "extra1" to metadataClass {
                + "extra2" to metadataClass {
                    + "extra3" to metadataClass {
                        + "prop10" to PrimitiveString
                    }
                }
            }
        }
        val source = metadataClass {
            + "prop1" to PrimitiveString
            + "prop2" to PrimitiveString
            + "prop3" to PrimitiveString
            + "prop4" to metadataClass {
                + "prop5" to PrimitiveString
                + "prop6" to PrimitiveString
                + "prop7" to metadataClass {
                    + "prop8" to PrimitiveString
                    + "prop9" to PrimitiveString
                }
            }
            + "prop10" to PrimitiveString
        }
        Assertions.assertEquals(expected, source.transformTo {
            "prop4.prop5" moveTo "prop5"
            "prop4.prop7.prop8" moveTo "prop8"
            "prop4.prop7.prop9" moveTo "prop4.prop9"
            "prop10" moveTo "extra1.extra2.extra3.prop10"
        }.destinationMetadata)
    }

}