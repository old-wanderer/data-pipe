package datapipe.core.pipeline

import datapipe.core.data.model.metadata.PrimitiveBoolean
import datapipe.core.data.model.metadata.PrimitiveLong
import datapipe.core.data.model.metadata.dsl.metadataClass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 17.02.2018
 */
class PipelineAliasForBadNameTest {

    @Test
    fun aliasCreateTest() {
        val sourceMetadata = metadataClass {
            + "good1" to PrimitiveLong
            + "/bad1" to PrimitiveBoolean
        }
        val expectedMetadata = metadataClass {
            + "good1" to PrimitiveLong
            + "bad1" or "/bad1" to PrimitiveBoolean
        }

        Assertions.assertEquals(expectedMetadata, (sourceMetadata + Pipelines.aliasForBadNames()).value)
    }

    @Test
    fun alias2CreateTest() {
        val sourceMetadata = metadataClass {
            + "good1" to PrimitiveLong
            + "/bad1" or "/some_bad_name" to PrimitiveBoolean
        }
        val expectedMetadata = metadataClass {
            + "good1" to PrimitiveLong
            + "bad1" or "/bad1" or "/some_bad_name" to PrimitiveBoolean
        }

        Assertions.assertEquals(expectedMetadata, (sourceMetadata + Pipelines.aliasForBadNames()).value)
    }

}