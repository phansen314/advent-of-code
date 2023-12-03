package com.pbh.soft.common.parsing

import cc.ekblad.konbini.Parser
import cc.ekblad.konbini.ParserResult
import cc.ekblad.konbini.parse
import org.slf4j.Logger

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

  inline fun <T, R> ParseLinesResult<T>.onSuccess(block: (List<T>) -> R): R =
    if (errors.isNotEmpty()) throw ParsingError(errors)
    else block(values)
}