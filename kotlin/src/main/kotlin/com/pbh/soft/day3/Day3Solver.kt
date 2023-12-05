package com.pbh.soft.day3

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Col
import com.pbh.soft.common.grid.HasColumn
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.Row
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.common.parsing.ParsingUtils.parseSparseGrid
import com.pbh.soft.common.parsing.ParsingUtils.withPos
import com.pbh.soft.day3.Parsing.lineP
import com.pbh.soft.day3.Parsing.numberRegex
import com.pbh.soft.day3.Parsing.symbolRegex
import mu.KLogging

object Day3Solver : Solver, KLogging() {
  override fun solveP1(text: String): String {
    return lineP.parseSparseGrid(text).onSuccess { grid ->
      val symbols = grid.findAll { it.parsed.matches(symbolRegex) }
      val rowXintervals = grid.findAllByRow { it.parsed.matches(numberRegex) }.associate { (row, colXcells) ->
        row to Intervals().apply {
          colXcells.forEach { (_, cell) -> this[Interval(cell.column, cell.column + cell.parsed.length - 1, row)] = cell.parsed.toInt() }
        }
      }

      symbols.asSequence()
        .flatMap { (loc, _) -> adjacentNumbers(loc, rowXintervals) }
        .sum()
        .toString()
    }
  }

  override fun solveP2(text: String): String {
    return lineP.parseSparseGrid(text).onSuccess { grid ->
      val symbols = grid.findAll { it.parsed == "*" }
      val rowXintervals = grid.findAllByRow { it.parsed.matches(numberRegex) }.associate { (row, colXcells) ->
        row to Intervals().apply {
          colXcells.forEach { (_, cell) -> this[Interval(cell.column, cell.column + cell.parsed.length - 1, row)] = cell.parsed.toInt() }
        }
      }

      symbols.asSequence()
        .map { (loc, _) -> adjacentNumbers(loc, rowXintervals) }
        .filter { it.size == 2 }
        .map { (a, b) -> a * b}
        .sum()
        .toString()
    }
  }

  private fun adjacentNumbers(loc: Loc, rowXintervals: Map<Row, Intervals>): List<Int> {
    return loc.neighbors()
      .map { (r, c) -> rowXintervals[r]?.let { it[c] }
      }
      .filterNotNull()
      .distinctBy { it.first }
      .map { it.second }
      .toList()
  }
}

object Parsing {
  val numberRegex = Regex("[0-9]+")
  val symbolRegex = Regex("[^.\\d]")

  val cellP = oneOf(regex(numberRegex).withPos(), regex(symbolRegex).withPos()).map { (i, s) -> Cell(i, s) }
  val blankP = many(char('.'))
  val lineP = many(bracket(blankP, blankP, cellP))
}

data class Cell(override val column: Col, val parsed: String) : HasColumn

data class Interval(val start: Int, val endInclusive: Int, val row: Int) : Comparable<Interval> {
  constructor(start: Int, row: Int) : this(start, start, row)

  override fun compareTo(other: Interval): Int =
    if (other.endInclusive < start) -1
    else if (endInclusive < other.start) 1
    else 0
}

class Intervals {
  private val intervalTree = sortedMapOf<Interval, Pair<Interval, Int>>()

  operator fun set(interval: Interval, value: Int) {
    intervalTree[interval] = interval to value
  }

  operator fun get(interval: Interval): Pair<Interval, Int>? = intervalTree[interval]
  operator fun get(column: Int): Pair<Interval, Int>? = intervalTree[Interval(column, column)]

  operator fun contains(column: Int): Boolean = intervalTree.containsKey(Interval(column, column))
}