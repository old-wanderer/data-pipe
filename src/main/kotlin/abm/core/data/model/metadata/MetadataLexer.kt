package abm.core.data.model.metadata

import java.io.BufferedReader
import java.io.StringReader
import java.util.*

/**
 * @author: Andrei Shlykov
 * @since: 28.02.2018
 */
class MetadataLexer(metadata: String) {

    private val reader = BufferedReader(StringReader(metadata))
    private val buffer = LinkedList<Char>()

    private val tokenMapper = mapOf(
            "{" to ObjectBegin,
            "}" to ObjectEnd,
            "[" to ListBegin,
            "]" to ListEnd,
            "|" to AliasSeparator,
            ":" to TypeSeparator,
            "\uFFFF" to EOFToken,

            "PrimitiveString" to PrimitiveToken(PrimitiveString),
            "PrimitiveLong" to PrimitiveToken(PrimitiveLong),
            "PrimitiveDouble" to PrimitiveToken(PrimitiveDouble),
            "PrimitiveBoolean" to PrimitiveToken(PrimitiveBoolean)
    )

    fun nextToken(): MetadataToken {
        skipWhiteSpace()


        val word = nextWord()
        return tokenMapper[word] ?: PropertyNameToken(word)
    }

    private fun nextWord(): String {
        val first = peekChar()
        return if (first.isSpecSymbol()) {
            nextChar()
            first.toString()
        } else {
            buildString {
                do {
                    append(peekChar())
                    nextChar()
                } while (!peekChar().isSpecSymbol() && !peekChar().isWhitespace())
            }
        }
    }

    private fun skipWhiteSpace() {
        while (peekChar().isWhitespace())
            nextChar()
    }

    private fun nextChar() {
        if (buffer.isNotEmpty())
            buffer.poll()
        else
            reader.read()
    }

    private fun peekChar(): Char {
        if (buffer.isEmpty()) {
            buffer.add(reader.read().toChar())
        }
        return buffer.first
    }

    private fun Char.isSpecSymbol() = this in listOf('{', '}', '[', ']', '|', ':', '\uFFFF')

}