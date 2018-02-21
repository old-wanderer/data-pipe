package abm.core.data.model.metadata

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 19.02.2018
 */
class MetadataBuilderTest {

    @Test
    fun buildMetadataClassWithAliases() {
        val expected = MetadataClass(setOf(PropertyMetadata("prop1", PrimitiveString, setOf("/badName1"))))
        val metadata = metadataClass {
            + ("prop1" or "/badName1" to PrimitiveString)
        }

        Assertions.assertEquals(expected, metadata)
    }

}