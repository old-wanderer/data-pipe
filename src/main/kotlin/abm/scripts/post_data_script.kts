import abm.core.data.handler.DataHandler
import java.io.InputStream

/*
 * К сожалению не работает. Ребята из JetBrains как-то не очень активно правят багу
 * https://youtrack.jetbrains.com/issue/KT-11618
 */

val DATA_PREFIX = "json_data/SEC_Rulemaking_"
val POSTS_DATA = "${DATA_PREFIX}posts.json"

fun <R> readFromResourceAndExecute(path: String, process: (InputStream) -> R): R =
    String.javaClass.classLoader.getResourceAsStream(path).use(process)


val model = readFromResourceAndExecute(POSTS_DATA) { DataHandler.extractDataModel(it, 5) }

print(model)