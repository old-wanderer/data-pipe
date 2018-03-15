package datapipe.core.pipeline

import datapipe.core.data.model.metadata.*
import datapipe.core.pipeline.Pipelines
import datapipe.core.pipeline.plus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * @author: Andrei Shlykov
 * @since: 25.02.2018
 */
class PipelineRemoveUnnecessaryPropertiesTest {

    @Test
    fun notNeedRemove() {
        val source = metadataClass {
            +PrimitiveString
            +PrimitiveDouble
            + metadataClass {
                +PrimitiveString
                +PrimitiveLong
                + metadataClass {
                    +PrimitiveString
                    +PrimitiveString
                }
            }
            + metadataList(PrimitiveString)
            + metadataList(metadataClass {
                +PrimitiveString
                +PrimitiveString
            })
        }

        Assertions.assertEquals(source, (source + Pipelines.removeUnnecessaryProperties()).value)
    }

    @Test
    fun emptyObjectsRemove() {
        val source = metadataClass {
            + ("p0" to PrimitiveString)
            + ("p1" to PrimitiveDouble)
            + ("p2" to metadataClass {  })
            + ("p3" to metadataClass {
                +PrimitiveString
            })
            + ("p4" to metadataClass {
                +PrimitiveString
                + metadataClass {  }
            })
            + ("p5" to metadataClass {
                + metadataClass {  }
            })
        }
        val expected = metadataClass {
            + ("p0" to PrimitiveString)
            + ("p1" to PrimitiveDouble)
            + ("p3" to metadataClass {
                +PrimitiveString
            })
            + ("p4" to metadataClass {
                +PrimitiveString
            })
        }

        Assertions.assertEquals(expected, (source + Pipelines.removeUnnecessaryProperties()).value)
    }

    @Test
    fun emptyListRemove() {
        val source = metadataClass {
            + ("p0" to PrimitiveString)
            + ("p1" to PrimitiveDouble)
            + ("p2" to metadataList())
            + ("p3" to metadataList(PrimitiveString))
            + ("p4" to metadataList(metadataClass {  }))
            + ("p5" to metadataList(metadataClass {
                + metadataClass {  }
            }))
        }
        val expected = metadataClass {
            + ("p0" to PrimitiveString)
            + ("p1" to PrimitiveDouble)
            + ("p3" to metadataList(PrimitiveString))
        }

        Assertions.assertEquals(expected, (source + Pipelines.removeUnnecessaryProperties()).value)
    }

}