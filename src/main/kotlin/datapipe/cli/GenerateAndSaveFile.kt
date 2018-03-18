package datapipe.cli

import datapipe.core.data.generator.ClassGenerator
import datapipe.core.data.model.metadata.*

/**
 * @author: Andrei Shlykov
 * @since: 07.03.2018
 */


fun main(args: Array<String>) {
    val metadata = metadataClass {
        + PrimitiveLong
        + PrimitiveString
        + metadataList(PrimitiveDouble)
        + metadataClass {
            + PrimitiveLong
            + PrimitiveString
            + metadataList(PrimitiveDouble)
        }
    }

    ClassGenerator.generateClassAndSave(metadata, "./work_output/GenerateClassAndSave/Example0.class")

}