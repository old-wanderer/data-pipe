package abm.core.data.model.metadata.parser

import abm.core.data.model.metadata.MetadataClass
import abm.core.data.model.metadata.MetadataList
import abm.core.data.model.metadata.MetadataPrimitive
import kotlin.coroutines.experimental.buildSequence

/**
 * @author: Andrei Shlykov
 * @since: 04.03.2018
 */

/**
 * токенизирует сериализированные метаданные в строковом виде
 *
 * @param str сериализированные метаданные в строковом виде
 * @return список токенов [MetadataToken]
 */
fun tokenize(str: String): List<MetadataToken> {
    val lexer = MetadataLexer(str)
    val result = mutableListOf<MetadataToken>()
    while (result.lastOrNull() != EOFToken) {
        result.add(lexer.nextToken())
    }
    return result
}

/**
 * токенезирует метаданные
 *
 * @param metadataClass метаданные
 * @return возвращает последовательность токенов [MetadataToken]
 */
fun tokenize(metadataClass: MetadataClass): Sequence<MetadataToken> = buildSequence {
    yield(ObjectBegin)
    for (property in metadataClass.properties) {

        yield(PropertyNameToken(property.name))
        if (property.aliasNames.isNotEmpty()) {
            for (alias in property.aliasNames) {
                yield(AliasSeparator)
                yield(PropertyNameToken(alias))
            }
        }
        yield(TypeSeparator)

        when (property.type) {
            is MetadataPrimitive -> yield(PrimitiveToken(property.type))
            is MetadataClass -> yieldAll(tokenize(property.type))
            is MetadataList  -> {
                yield(ListBegin)
                when (property.type.containsType) {
                    is MetadataPrimitive -> yield(PrimitiveToken(property.type.containsType))
                    is MetadataClass -> yieldAll(tokenize(property.type.containsType))
                    else -> throw RuntimeException("MetadataList contains unresolved type")
                }
                yield(ListEnd)
            }
        }

    }
    yield(ObjectEnd)
}
