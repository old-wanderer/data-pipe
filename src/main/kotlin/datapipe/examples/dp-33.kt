package datapipe.examples

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.model.metadata.*
import datapipe.core.pipeline.Pipelines
import com.google.gson.Gson
import java.io.BufferedWriter
import java.io.FileWriter

/**
 * @author: Andrei Shlykov
 * @since: 28.02.2018
 */

val finish_metadata = metadataClass {
    + "decision" to metadataList(PrimitiveString)
    + "location" to metadataList(PrimitiveString)
    + "organization" to metadataList(PrimitiveString)
    + "person" to metadataList(metadataClass {
        + "name" to PrimitiveString
        + "category" to PrimitiveString
        + "grade" to PrimitiveString
        + "location" to PrimitiveString
        + "occupation" to PrimitiveString
        + "pay_plan" to PrimitiveString
        + "salary" to PrimitiveString
    })
}

val finish_class = finish_metadata.generatedClass
val agentClass = ((finish_metadata.properties.find { it.name == "person" }!!.type as MetadataList)
        .containsType as MetadataClass).generatedClass

fun transformDataStep2(oldObj: GeneratedClass, indexedAgents: Map<String, GeneratedClass>): GeneratedClass {
    val finish_dec_setter = finish_class.getField("decision")::set
    val finish_loc_setter = finish_class.getField("location")::set
    val finish_org_setter = finish_class.getField("organization")::set
    val finish_person_setter = finish_class.getField("person")::set

    val agent_name_setter = agentClass.getField("name")::set
    val agent_grad_setter = agentClass.getField("grade")::set
    val agent_loca_setter = agentClass.getField("location")::set
    val agent_occu_setter = agentClass.getField("occupation")::set
    val agent_payp_setter = agentClass.getField("pay_plan")::set
    val agent_sala_setter = agentClass.getField("salary")::set
    val agent_cate_setter = agentClass.getField("category")::set

    @Suppress("UNCHECKED_CAST")
    val personNames = (oldObj.getPropertyValue("person")  ?: emptyList<String>()) as List<String>
    val agents = personNames.map {
        val agent = agentClass.newInstance()
        if (it in indexedAgents) {
            agent_cate_setter(agent, "sec_commission")
            agent_name_setter(agent, it)
            agent_grad_setter(agent, indexedAgents[it]!!.getPropertyValue("grade"))
            agent_loca_setter(agent, indexedAgents[it]!!.getPropertyValue("location"))
            agent_occu_setter(agent, indexedAgents[it]!!.getPropertyValue("occupation"))
            agent_payp_setter(agent, indexedAgents[it]!!.getPropertyValue("pay_plan"))
            agent_sala_setter(agent, indexedAgents[it]!!.getPropertyValue("salary"))
        } else {
            agent_cate_setter(agent, "other")
            agent_name_setter(agent, it)
        }
        agent
    }

    val newObj = finish_class.newInstance() as GeneratedClass
    finish_dec_setter(newObj, oldObj.getPropertyValue("decision"))
    finish_loc_setter(newObj, oldObj.getPropertyValue("location"))
    finish_org_setter(newObj, oldObj.getPropertyValue("organization"))
    finish_person_setter(newObj, agents)

    return newObj
}

fun scenario2(path: String) =
        (Pipelines.extractModelFrom(path, 10)
                + Pipelines.removeUnnecessaryProperties()
                + Pipelines.aliasForBadNames()
                + Pipelines.excludeNamesFromMetadata(
                    "related_to", "rule_names", "stats", "source_subcategory",
                    "meta", "content", "document_url", "agent_ids",
                    "header", "links", "source_url")
                + Pipelines.process(System.out::println)
                + Pipelines.generateClass()
                + Pipelines.parseData(path))

fun main(args: Array<String>) {
    val pipeline = scenario2("/Users/andrei/Downloads/batsbzx_agents_v3.json")
    val agentsRepo = scenario2("/Users/andrei/Downloads/results.txt").value

    val nameIndexedAgent = agentsRepo.map { it.getPropertyValue("name") as String to it }.toMap()

    val transformValues1 = pipeline.value.transform {
        "names_entities.LOCATION" moveTo "location"
        "names_entities.ORGANIZATION" moveTo "organization"
        "names_entities.PERSON" moveTo "person"
    }
    val transformValues2 = transformValues1.map { transformDataStep2(it, nameIndexedAgent) }

    val writer = BufferedWriter(FileWriter("./work_output/dp-33/result.txt"))
    val gson = Gson()
    for (data in transformValues2) {
        writer.write(gson.toJson(data))
        writer.newLine()
    }
}