package com.pbh.soft.day11

import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.Loc.Companion.manhattanDistance
import com.pbh.soft.common.grid.Row
import com.pbh.soft.common.parsing.ParsingUtils
import com.pbh.soft.common.parsing.ParsingUtils.locP
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.or
import com.pbh.soft.kparse.KParser.Companion.then
import mu.KLogging
import java.math.BigInteger
import kotlin.math.abs

object Day11Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.problemP.onSuccess(text) { grid ->
    val galaxies = mutableListOf<Loc>()
    val blankRows = sortedSetOf<Row>()
    var isColumnBlank = grid.cIndices.mapTo(mutableListOf()) { true }
    for (r in 0..<grid.rSize) {
      var isRowBlank = true
      for (c in 0..<grid.cSize) {
        val universe = grid[r, c]!!
        if (universe is Universe.Galaxy) {
          isRowBlank = false
          isColumnBlank[c] = false
          galaxies.add(Loc(r, c))
        }
      }
      if (isRowBlank)
        blankRows.add(r)
    }
    val blankColumns = isColumnBlank.asSequence().withIndex().filter { it.value }.map { it.index }.toSortedSet()
    val numRowsExpanded = Array(grid.rSize) { 0 }
    for (r in 1..<grid.rSize) {
      val delta = if (r in blankRows) 1 else 0
      numRowsExpanded[r] = numRowsExpanded[r - 1] + delta
    }
    val numColsExpanded = Array(grid.cSize) { 0 }
    for (c in 1..<grid.cSize) {
      val delta = if (c in blankColumns) 1 else 0
      numColsExpanded[c] = numColsExpanded[c - 1] + delta
    }

    var total = 0
    for (i in galaxies.indices) {
      for (j in (i + 1)..<galaxies.size) {
        val locI = galaxies[i]
        val locJ = galaxies[j]
        val dist = manhattanDistance(locI, locJ)
        val deltaR = abs(numRowsExpanded[locI.r] - numRowsExpanded[locJ.r])
        val deltaC = abs(numColsExpanded[locI.c] - numColsExpanded[locJ.c])
        val updatedDist = dist + deltaR + deltaC
        total += updatedDist
      }
    }

    return total.toString()
  }

  override fun solveP2(text: String): String = Parsing.problemP.onSuccess(text) { grid ->
    val galaxies = mutableListOf<Loc>()
    val blankRows = sortedSetOf<Row>()
    var isColumnBlank = grid.cIndices.mapTo(mutableListOf()) { true }
    for (r in 0..<grid.rSize) {
      var isRowBlank = true
      for (c in 0..<grid.cSize) {
        val universe = grid[r, c]!!
        if (universe is Universe.Galaxy) {
          isRowBlank = false
          isColumnBlank[c] = false
          galaxies.add(Loc(r, c))
        }
      }
      if (isRowBlank)
        blankRows.add(r)
    }
    val blankColumns = isColumnBlank.asSequence().withIndex().filter { it.value }.map { it.index }.toSortedSet()
    val numRowsExpanded = Array(grid.rSize) { 0 }
    for (r in 1..<grid.rSize) {
      val delta = if (r in blankRows) 999999 else 0
      numRowsExpanded[r] = numRowsExpanded[r - 1] + delta
    }
    val numColsExpanded = Array(grid.cSize) { 0 }
    for (c in 1..<grid.cSize) {
      val delta = if (c in blankColumns) 999999 else 0
      numColsExpanded[c] = numColsExpanded[c - 1] + delta
    }

    var total = BigInteger.ZERO
    for (i in galaxies.indices) {
      for (j in (i + 1)..<galaxies.size) {
        val locI = galaxies[i]
        val locJ = galaxies[j]
        val dist = manhattanDistance(locI, locJ)
        val deltaR = abs(numRowsExpanded[locI.r] - numRowsExpanded[locJ.r])
        val deltaC = abs(numColsExpanded[locI.c] - numColsExpanded[locJ.c])
        val updatedDist = dist + deltaR + deltaC
        total += updatedDist.toBigInteger()
      }
    }

    return total.toString()
  }
}

sealed class Universe {
  data object Space : Universe()
  data class Galaxy(val loc: Loc) : Universe()
}

data class Problem(val galaxies: MutableList<Loc>, val numBlankRows: Set<Row>, val numBlankCols: Set<Int>)

object Parsing {
  val spaceP: KParser<Universe> = chr('.').map { Universe.Space }
  val galaxyP: KParser<Universe> = locP.then(chr('#')).map { Universe.Galaxy(it.first) }
  val problemP = ParsingUtils.gridP(spaceP.or(galaxyP))
//  val problemP = parser {
//    var maxR: Int = 0
//    var maxC: Int = 0
//    val galaxies = mutableListOf<Loc>()
//    var isColumnBlank = mutableListOf<Boolean>()
//    val blankRows = sortedSetOf<Row>()
//    var r = 0
//    while (isNotDone()) {
//      val row = many(spaceP.or(galaxyP)); opt(newline)
//
//      if (isColumnBlank.isEmpty()) {
//        isColumnBlank = row.indices.mapTo(mutableListOf()) { true }
//      }
//      var rowBlank = true
//      for ((c, universe) in row.withIndex()) {
//        if (universe is Universe.Galaxy) {
//          rowBlank = false
//          galaxies.add(universe.loc)
//          isColumnBlank[c] = false
//        }
//      }
//      if (rowBlank)
//        blankRows.add(r)
//      r++
//    }
//    val blankCols = isColumnBlank.asSequence().withIndex().filter { it.value }.map { it.index }.toSortedSet()
//    Problem(galaxies, blankRows, blankCols)
//  }
}