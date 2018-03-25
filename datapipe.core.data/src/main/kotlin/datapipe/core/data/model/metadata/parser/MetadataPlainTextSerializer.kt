package datapipe.core.data.model.metadata.parser

import datapipe.core.data.model.metadata.MetadataClass

/**
 * @author: Andrei Shlykov
 * @since: 07.03.2018
 */

fun serialize(metadata: MetadataClass) = buildString {

    for (token in tokenize(metadata)) {
        val str = when (token) {
            ObjectBegin -> "{"
            ObjectEnd -> "} "

            ListBegin -> "["
            ListEnd -> "] "

            AliasSeparator ->"|"
            TypeSeparator -> ":"

            is PrimitiveToken -> token.type.toString() + " "
            is PropertyNameToken -> token.name
            EOFToken -> ""
        }
        append(str)
    }

}