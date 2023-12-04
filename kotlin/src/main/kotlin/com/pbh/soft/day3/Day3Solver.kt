package com.pbh.soft.day3

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.common.parsing.ParsingUtils.parseLinesIndexed
import com.pbh.soft.common.parsing.ParsingUtils.withPos
import com.pbh.soft.day3.Parsing.lineP
import mu.KLogging

object Day3Solver : Solver, KLogging() {
  override fun solveP1(text: String): String {
    return lineP.parseLinesIndexed(text).onSuccess { indexXlines ->
      val allSymbols = mutableListOf<Symbol>()
      val rowXintervals = mutableMapOf<Row, Intervals>()
      indexXlines.asSequence()
        .forEach { (row, cells) ->
          val intervals = Intervals()
          cells.asSequence()
            .forEach { cell ->
              when (cell) {
                is IntervalCell -> intervals[Interval(cell.column, cell.columnEndInclusive, row)] = cell.value
                is SymbolCell -> allSymbols.add(Symbol(cell.symbol, Loc(row, cell.column)))
              }
            }
          rowXintervals[row] = intervals
        }

      allSymbols.asSequence()
        .flatMap { (_, loc) -> adjacentNumbers(loc, rowXintervals) }
        .sum()
        .toString()
    }
  }

  override fun solveP2(text: String): String {
    return lineP.parseLinesIndexed(text).onSuccess { indexXlines ->
      val allSymbols = mutableListOf<Symbol>()
      val rowXintervals = mutableMapOf<Row, Intervals>()
      indexXlines.asSequence()
        .forEach { (row, cells) ->
          val intervals = Intervals()
          cells.asSequence()
            .forEach { cell ->
              when (cell) {
                is IntervalCell -> intervals[Interval(cell.column, cell.columnEndInclusive, row)] = cell.value
                is SymbolCell -> allSymbols.add(Symbol(cell.symbol, Loc(row, cell.column)))
              }
            }
          rowXintervals[row] = intervals
        }

      allSymbols.asSequence()
        .filter { (c, _) -> c == '*' }
        .map { (_, loc) -> adjacentNumbers(loc, rowXintervals) }
        .filter { it.size == 2 }
        .map { (a, b) -> a * b }
        .sum()
        .toString()
    }

  }

  /*
   *   N
   * W   E
   *   S
   */
  private fun adjacentNumbers(loc: Loc, rowXintervals: Map<Row, Intervals>): List<Int> {
    val nums =
      ((loc.r - 1)..(loc.r + 1)).asSequence()
        .flatMap { r ->
          ((loc.c - 1)..(loc.c + 1)).asSequence().map { c -> Loc(r, c) }
        }
        .map { (r, c) ->
          rowXintervals[r]?.let { it[c] }
        }
        .filterNotNull()
        .distinctBy { it.first }
        .map { it.second }
        .toList()
    return nums
  }

//    //--
//    sequenceOf(
//      Loc(loc.r - 1, loc.c - 1), Loc(loc.r - 1, loc.c), Loc(loc.r - 1, loc.c + 1),
//      Loc(loc.r, loc.c - 1), Loc(loc.r, loc.c + 1),
//      Loc(loc.r + 1, loc.c - 1), Loc(loc.r + 1, loc.c), Loc(loc.r + 1, loc.c + 1))
//    //--
//    val northIntervals = rowXintervals[loc.r - 1]
//    if (northIntervals != null) {
//      val northNum = northIntervals[loc.c]
//      val x = ((loc.c - 1)..(loc.c + 1)).asSequence().map { northIntervals[it] }.filterNotNull().distinct()
//        .map { northIntervals[it] }.filterNotNull()
//
//      if (northNum != null) add(northNum)
//      else listOf(loc.c - 1, loc.c + 1)
//    }
//  }
}

class SparseGrid<T>(val locXelements: Map<Loc, T>) {
  companion object {
    operator fun <T> invoke(block: SparseGridDsl<T>.() -> Unit): SparseGrid<T> = SparseGridDsl<T>().apply(block).build()
  }

  operator fun get(location: Loc): T? = locXelements[location]

  operator fun contains(location: Loc): Boolean = locXelements.containsKey(location)

  val entries: Set<Map.Entry<Loc, T>> get() = locXelements.entries

  @Suppress("UNCHECKED_CAST")
  inline fun <reified U : T> entriesByValueType(): Sequence<Map.Entry<Loc, U>> =
    locXelements.asSequence().filter { it.value is U } as Sequence<Map.Entry<Loc, U>>
}

class SparseGridDsl<T> {
  private val locXelements = mutableMapOf<Loc, T>()
  val grid = this

  fun build() = SparseGrid(locXelements)

  operator fun set(location: Loc, element: T) {
    locXelements[location] = element
  }
}

data class Loc(val r: Int, val c: Int)
data class Symbol(val symbol: Char, val loc: Loc)
data class Interval(val start: Int, val endInclusive: Int, val row: Int) : Comparable<Interval> {
  constructor(start: Int, row: Int) : this(start, start, row)

  override fun compareTo(other: Interval): Int =
    if (other.endInclusive < start) -1
    else if (endInclusive < other.start) 1
    else 0
}

typealias Row = Int

class Intervals {
  private val intervalTree = sortedMapOf<Interval, Pair<Interval, Int>>()

  operator fun set(interval: Interval, value: Int) {
    intervalTree[interval] = interval to value
  }

  operator fun get(interval: Interval): Pair<Interval, Int>? = intervalTree[interval]
  operator fun get(column: Int): Pair<Interval, Int>? = intervalTree[Interval(column, column)]

  operator fun contains(column: Int): Boolean = intervalTree.containsKey(Interval(column, column))
}

object Parsing {
  val numberRegex = Regex("[0-9]+")
  val symbolRegex = Regex("[^.\\d]")

  val numberP = regex(numberRegex).withPos().map { (i, s) -> IntervalCell(i, i + s.length - 1, s.toInt()) }
  val symbolP = regex(symbolRegex).withPos().map { (i, s) -> SymbolCell(s[0], i) }
  val cellP = oneOf(numberP, symbolP)
  val blankP = many(char('.'))
  val lineP = many(bracket(blankP, blankP, cellP))
}

sealed class Cell {
  abstract val column: Int
}

data class SymbolCell(val symbol: Char, override val column: Int) : Cell()
data class IntervalCell(override val column: Int, val columnEndInclusive: Int, val value: Int) : Cell()

