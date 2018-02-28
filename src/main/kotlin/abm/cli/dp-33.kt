package abm.cli

import abm.core.data.generator.GeneratedClass
import abm.core.data.model.metadata.*
import abm.core.data.pipeline.PipelineElement
import abm.core.data.pipeline.Pipelines
import abm.core.data.pipeline.plus

/**
 * @author: Andrei Shlykov
 * @since: 28.02.2018
 */

fun dfsRemovePropIfName(node: MetadataAstNode, names: List<String>) {
    node.children.removeIf {
        dfsRemovePropIfName(it, names)
        it is MetadataPropertyNode && it.name in names
    }
}

fun removePropIfName(vararg names: String) =
        PipelineElement<Metadata, Metadata>({ source ->
            if (source is MetadataClass) {
                val ast = buildMetadataAstTree(metadataTokens(source).toList())
                dfsRemovePropIfName(ast, names.toList())
                return@PipelineElement buildMetadata(ast)
            }
            source!!
        })

val result_metadata = metadataClass {
    + ("decision" to metadataList(PrimitiveString))
    + ("location" to metadataList(PrimitiveString))
    + ("organization" to metadataList(PrimitiveString))
    + ("person" to metadataList(PrimitiveString))
}

val result_class = (result_metadata + Pipelines.generateClass()).value as Class<*>


fun transformData(oldObj: Any): GeneratedClass {
    oldObj as GeneratedClass

    val dec_setter = result_class.getField("decision")::set
    val loc_setter = result_class.getField("location")::set
    val org_setter = result_class.getField("organization")::set
    val per_setter = result_class.getField("person")::set

    val newObj = result_class.newInstance() as GeneratedClass
    dec_setter(newObj, oldObj.getPropertyValue("decision"))
    loc_setter(newObj, oldObj.getPropertyValue("names_entities.LOCATION"))
    org_setter(newObj, oldObj.getPropertyValue("names_entities.ORGANIZATION"))
    per_setter(newObj, oldObj.getPropertyValue("names_entities.PERSON"))


    return newObj
}

fun scenario2(path: String) =
        (Pipelines.extractModelFrom(path, 10)
                + Pipelines.removeUnnecessaryProperties()
                + Pipelines.aliasForBadNames()
                + removePropIfName(
                    "related_to", "rule_names", "stats", "source_subcategory",
                    "meta", "content", "document_url", "agent_ids",
                    "header", "links", "source_url")
                + Pipelines.process(System.out::println)
                + Pipelines.generateClass()
                + Pipelines.parseData(path))

fun main(args: Array<String>) {
    val pipeline = scenario2("/Users/andrei/Downloads/batsbzx_agents_v3.json")

    val transformValues = pipeline.value.map(::transformData)

    println(transformValues.joinToString("\n", transform = { "${it.getPropertyValue("decision")}; ${it.getPropertyValue("person")};" }))
}