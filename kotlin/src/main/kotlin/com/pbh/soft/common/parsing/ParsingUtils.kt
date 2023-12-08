package com.pbh.soft.common.parsing

import cc.ekblad.konbini.*
import com.pbh.soft.common.grid.HasColumn
import com.pbh.soft.common.grid.SparseGrid
import com.pbh.soft.day7.Parsing

object ParsingUtils {
  fun <T> Parser<T>.parseLines(text: String): ParseLinesResult<List<T>> {
    val (ts, errors) = mutableListOf<T>() to mutableListOf<ParseLineError>()
    for ((lineNum, line) in text.lineSequence().withIndex()) {
      when (val pResult = parse(line)) {
        is ParserResult.Error -> errors.add(ParseLineError(lineNum, pResult))
        is ParserResult.Ok -> ts.add(pResult.result)
      }
    }

    return ParseLinesResult(ts, errors)
  }

  fun <T> Parser<T>.parseLinesIndexed(text: String): ParseLinesResult<List<Pair<Int, T>>> {
    val (ts, errors) = mutableListOf<Pair<Int, T>>() to mutableListOf<ParseLineError>()
    for ((lineNum, line) in text.lineSequence().withIndex()) {
      when (val pResult = parse(line)) {
        is ParserResult.Error -> errors.add(ParseLineError(lineNum, pResult))
        is ParserResult.Ok -> ts.add(lineNum to pResult.result)
      }
    }

    return ParseLinesResult(ts, errors)
  }

  fun <T : HasColumn> Parser<List<T>>.parseSparseGrid(text: String): ParseLinesResult<SparseGrid<T>> {
    val errors = mutableListOf<ParseLineError>()
    val grid = SparseGrid {
      for ((lineNum, line) in text.lineSequence().withIndex()) {
        when (val pResult = parse(line)) {
          is ParserResult.Error -> errors.add(ParseLineError(lineNum, pResult))
          is ParserResult.Ok -> pResult.result.forEach { t -> grid[lineNum, t.column] = t }
        }
      }
    }

    return ParseLinesResult(grid, errors)
  }

  inline fun <T, R> ParseLinesResult<T>.onSuccess(block: (T) -> R): R =
    if (errors.isNotEmpty()) throw ParsingLinesError(errors)
    else block(values)

  inline fun <T, R> ParserResult<T>.onSuccess(block: (T) -> R): R =
    when (this) {
      is ParserResult.Error -> throw ParsingError(this)
      is ParserResult.Ok -> block(result)
    }

  fun <T> Parser<T>.withPos(): Parser<Pair<Int, T>> = parser { position to this@withPos() }

  fun <T> parseMap(map: Map<Char, T>): Parser<T> = parser {
    val c = char()
    map[c] ?: fail("char '$c' not found in map!")
  }

  val intP = integer.map(Long::toInt)
  val newlineP = string("\r\n")

}