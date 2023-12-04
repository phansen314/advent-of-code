package com.pbh.soft.common.parsing

import cc.ekblad.konbini.*

object ParsingUtils {
  fun <T> Parser<T>.parseLines(text: String): ParseLinesResult<T> {
    val (ts, errors) = mutableListOf<T>() to mutableListOf<ParseLineError>()
    for ((lineNum, line) in text.lineSequence().withIndex()) {
      when (val pResult = parse(line)) {
        is ParserResult.Error -> errors.add(ParseLineError(lineNum, pResult))
        is ParserResult.Ok -> ts.add(pResult.result)
      }
    }

    return ParseLinesResult(ts, errors)
  }

  fun <T> Parser<T>.parseLinesIndexed(text: String): ParseLinesResult<Pair<Int, T>> {
    val (ts, errors) = mutableListOf<Pair<Int, T>>() to mutableListOf<ParseLineError>()
    for ((lineNum, line) in text.lineSequence().withIndex()) {
      when (val pResult = parse(line)) {
        is ParserResult.Error -> errors.add(ParseLineError(lineNum, pResult))
        is ParserResult.Ok -> ts.add(lineNum to pResult.result)
      }
    }

    return ParseLinesResult(ts, errors)
  }

  inline fun <T, R> ParseLinesResult<T>.onSuccess(block: (List<T>) -> R): R =
    if (errors.isNotEmpty()) throw ParsingLinesError(errors)
    else block(values)

  inline fun <T, R> ParserResult<T>.onSuccess(block: (T) -> R): R =
    when (this) {
      is ParserResult.Error -> throw ParsingError(this)
      is ParserResult.Ok -> block(result)
    }

  fun <T> Parser<T>.withPos(): Parser<Pair<Int, T>> = parser { position to this@withPos() }
}