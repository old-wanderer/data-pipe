package datapipe.cli

import datapipe.core.data.generator.GeneratedClass
import datapipe.core.data.pipeline.*
import java.lang.reflect.Field


/**
 * @author: andrei shlykov
 * @since: 21.01.2018
 */
const val DATA_PREFIX = "./analyze_data/SEC_Rulemaking_"
const val NEW_DATA = "./analyze_data/sec.rulemaking.json"
const val DOCUMENT_HISTORY_DATA = "${DATA_PREFIX}document_history.json"
const val DOCUMENTS_DATA = "${DATA_PREFIX}documents.json"
const val EVENT_HISTORY_DATA = "${DATA_PREFIX}event_history.json"
const val EVENTS_DATA = "${DATA_PREFIX}events.json"
const val POST_HISTORY_DATA = "${DATA_PREFIX}post_history.json"
const val POSTS_DATA = "${DATA_PREFIX}posts.json"


fun scenario1(path: String) =
        (Pipelines.extractModelFrom(path, 10)
        + Pipelines.removeUnnecessaryProperties()
        + Pipelines.aliasForBadNames()
        + Pipelines.process(System.out::println)
        + Pipelines.generateClass()
        + Pipelines.process { clazz ->
            if (clazz != null) {
                println(clazz.canonicalName)
                val maxLength = (clazz.fields. maxBy { it.name.length }?.name?.length ?: 0)+ 1
                for (field in clazz.fields.sortedBy(Field::getName)) {
                    println("\t%-${maxLength}s: %s".format(field.name, field.genericType.typeName))
                }
            }
        }
        + Pipelines.parseData(path)
        + Pipelines.process {
           println("unique values by field:")
           for ((key, value) in it!!.calcUniqueValue().entries) {
               println("\t$key: $value")
           }
        })


fun main(args: Array<String>) {

    val pipeline1 = scenario1(NEW_DATA)
    val pipeline2 = scenario1(EVENTS_DATA)

    val getterRep2 = pipeline2.value.containsClass.getField("document_url")::get
    val getterName = pipeline2.value.containsClass.getField("name")::get
    val getterCategory = pipeline2.value.containsClass.getField("category")::get
    val eventsIndex = pipeline2.value.groupBy(getterRep2) { getterName(it) to getterCategory(it) }

    val getterRep1 = pipeline1.value.containsClass.getField("document_url")::get
    val dataIndex = pipeline1.value
            .filter { getterRep1(it) in eventsIndex }
            .groupBy(getterRep1)

    println()
    println("-----------------------------------")
    println("decision;location;organization;person;[(name, category)];document_url;source_url;links")
    for (key in dataIndex.keys) {
        val data = dataIndex[key]!!.first()
        val event = eventsIndex[key]!!.distinct()
        println("${data.getPropertyValue("decision")};" +
                "${data.getPropertyValue("named_entities.LOCATION")};" +
                "${data.getPropertyValue("named_entities.ORGANIZATION")};" +
                "${data.getPropertyValue("named_entities.PERSON")};" +
                "$event;" +
                "${data.getPropertyValue("document_url")};" +
                "${data.getPropertyValue("source_url")};" +
                "${data.getPropertyValue("links")}"
        )
    }

}
