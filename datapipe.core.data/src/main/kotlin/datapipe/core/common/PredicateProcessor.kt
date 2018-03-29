package datapipe.core.common

/**
 * @author: Andrei Shlykov
 * @since: 29.03.2018
 */
class PredicateProcessor<in T, out R>(private val ops: List<Pair<(T) -> Boolean, (T) -> R>>) {

    constructor(predicate: (T) -> Boolean, processor: (T) -> R): this(listOf(predicate to processor))

    fun findProcessor(parameter: T) = ops.firstOrNull { it.first(parameter) }?.second

}