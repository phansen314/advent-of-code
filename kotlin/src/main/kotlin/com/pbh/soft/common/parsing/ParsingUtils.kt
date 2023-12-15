package com.pbh.soft.common.parsing

import cc.ekblad.konbini.*
import com.pbh.soft.common.grid.HasColumn
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.grid.SparseGrid
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.Result
import com.pbh.soft.kparse.State
import java.util.ArrayList
import kotlin.math.max

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

  inline fun <T, R> KParser<T>.onSuccess(text: String, block: (T) -> R): R =
    when (val res = this(State(text)).result) {
      is Result.Err -> throw IllegalStateException("parsing failed at ${res.position} due to ${res.message}")
      is Result.Ok -> block(res.value)
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
  val locP = KParser.pos.map { Loc(r = it.line, c = it.column) }

  fun <T> gridP(colParser: KParser<T>) = KParser.parser {
    var (maxR, maxC) = 0 to 0
    val rows = ArrayList<ArrayList<T>>()
    while (isNotDone()) {
      val cols = ArrayList<T>()
      val columns = many(colParser)
      if (columns.isEmpty()) break
      for ((c, t) in columns.withIndex()) {
        cols.add(t)
        maxC = max(maxC, c)
      }
      opt(KParser.newline)
      rows.add(cols)
      maxR++
    }

    MutableDenseGrid(rows)
  }

}