package datapipe.core.common

/**
 * @author: Andrei Shlykov
 * @since: 05.03.2018
 */
interface Visited<T> {

    fun visit(visitor: T)

}