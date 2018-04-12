package datapipe.examples

import datapipe.core.data.generator.ClassGenerator
import datapipe.core.data.model.metadata.*
import datapipe.core.data.model.metadata.dsl.*

/**
 * @author: Andrei Shlykov
 * @since: 07.03.2018
 */
fun main(args: Array<String>) {
    val metadata1 = metadataClass {
        + PrimitiveLong
        + PrimitiveString
        + metadataList(PrimitiveDouble)
        + metadataClass {
            + PrimitiveLong
            + PrimitiveString
            + metadataList(PrimitiveDouble)
        }
    }

    ClassGenerator.generateClassAndSave(metadata1, "./work_output/GenerateClassAndSave/Example1.class")

    val metadata2 = metadataClass {
        +"prop1" to PrimitiveLong default 11L
        +"prop2" to PrimitiveString default "prop2"
        +"prop3" to metadataClass {
            +"prop3_1" to PrimitiveLong default 31L
            +"prop3_2" to PrimitiveString default "prop3_2"
        }
    }

    ClassGenerator.generateClassAndSave(metadata2, "./work_output/GenerateClassAndSave/Example2.class")
}