package datapipe.core.pipeline

/**
 * @author: Andrei Shlykov
 * @since: 31.01.2018
 */

open class PipelineElement<P, V>(private val task: (P?) -> V) {

    open val value: V by lazy { task(previous?.value) }

    var previous: PipelineElement<*, P>? = null

    open operator fun <T> plus(other: PipelineElement<V, T>): PipelineElement<V, T> {
        other.previous = this
        return other
    }

    operator fun <R> plus(other: PipelineWithResult<V, R>): PipelineWithResult<V, R> {
        other.previous = this
        return other
    }

}

class PipelineWithResult<P, R>(private val resultTask: (P?) -> R): PipelineElement<P, P>( { it!! } ) {

    val result: R by lazy { resultTask(previous?.value) }

    operator fun <T> minus(other: PipelineResultHandler<P, T>): PipelineResultHandler<P, T> {
        other.previous = this
        return other
    }
}

class PipelineResultHandler<P, R>(private val resultHandler: (R?) -> Unit): PipelineElement<P, P>({ it!! }) {

    override val value: P by lazy {

        if (previous is PipelineWithResult<*, *>) {
            resultHandler((previous as PipelineWithResult<*, R>).result)
        }

        super.value
    }
}


infix fun <L, R> PipelineElement<*, L>.join(right: PipelineElement<*, R>): PipelineElement<Unit, Pair<L, R>> =
        PipelineElement { this.value to right.value }

operator fun <T> T.plus(right: PipelineElement<T, *>) = PipelineElement<T, T> { this } + right