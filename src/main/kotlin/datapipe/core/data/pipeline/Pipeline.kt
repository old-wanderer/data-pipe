package datapipe.core.data.pipeline

/**
 * @author: Andrei Shlykov
 * @since: 31.01.2018
 */

abstract class AbstractPipelineElement<P, V> {

    open val value: V by lazy { performTask(previous?.value) }

    protected var previous: AbstractPipelineElement<*, P>? = null

    protected abstract fun performTask(param: P?): V

    open operator fun <T> plus(other: AbstractPipelineElement<V, T>): AbstractPipelineElement<V, T> {
        other.previous = this
        return other
    }

}

open class PipelineElement<P, V>(private val task: (P?) -> V): AbstractPipelineElement<P, V>() {

    override fun performTask(param: P?): V = task(param)

}


infix fun <L, R> PipelineElement<*, L>.join(right: AbstractPipelineElement<*, R>): AbstractPipelineElement<Unit, Pair<L, R>> =
        PipelineElement { this.value to right.value }

operator fun <T> T.plus(right: AbstractPipelineElement<T, *>) = PipelineElement<T, T> { this } + right