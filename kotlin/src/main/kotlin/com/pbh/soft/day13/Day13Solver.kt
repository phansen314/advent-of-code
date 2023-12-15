package com.pbh.soft.day13

import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Col
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.grid.Row
import com.pbh.soft.common.parsing.ParsingUtils.gridP
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day13.Parsing.patternsP
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.newline
import com.pbh.soft.kparse.KParser.Companion.or
import mu.KLogging

object Day13Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = patternsP.onSuccess(text) { patterns ->
    var total = 0
    for (pattern in patterns) {
      val mirrorCol = pattern.findVerticalMirror()
      val mirrorRow = pattern.findHorizontalMirror()
      total += if (mirrorCol == -1) 0 else mirrorCol
      total += if (mirrorRow == -1) 0 else 100 * mirrorRow
    }

    total.toString()
  }

  override fun solveP2(text: String): String = patternsP.onSuccess(text) { patterns ->
    var total = 0
    for (pattern in patterns) {
      val mirrorCol = pattern.findVerticalMirrorPart2()
      val mirrorRow = pattern.findHorizontalMirrorPart2()
      total += if (mirrorCol == -1) 0 else mirrorCol
      total += if (mirrorRow == -1) 0 else 100 * mirrorRow
    }

    total.toString()
  }

  private fun <T> MutableDenseGrid<T>.rowsDiff(r: Row, otherR: Row): Int =
    cIndices.count { this[r, it] != this[otherR, it] }

  private fun <T> MutableDenseGrid<T>.colsDiff(c: Col, otherC: Col): Int =
    rIndices.count { this[it, c] != this[it, otherC] }

  private fun MutableDenseGrid<Char>.findHorizontalMirrorPart2(): Int {
    var mirrorRow = -1
    rowLoop@ for (r in 1..<rSize) {
      var rDiff = rowsDiff(r, r - 1)
      if (rDiff > 1) {
        continue@rowLoop
      }

      val rows = ((r - 2).downTo(0)).zip(r + 1..<rSize)
      for ((upperR, belowR) in rows) {
        rDiff += rowsDiff(upperR, belowR)
        if (rDiff > 1) {
          continue@rowLoop
        }
      }

      if (rDiff == 0) {
        continue@rowLoop
      }
      mirrorRow = r
      break@rowLoop
    }

    return mirrorRow
  }

  private fun MutableDenseGrid<Char>.findVerticalMirrorPart2(): Int {
    var mirrorCol = -1
    colLoop@ for (c in 1..<rSize) {
      var cDiff = colsDiff(c, c - 1)
      if (cDiff > 1) {
        continue@colLoop
      }

      val columns = ((c - 2).downTo(0)).zip(c + 1..<cSize)
      for ((leftC, rightC) in columns) {
        cDiff += colsDiff(leftC, rightC)
        if (cDiff > 1) {
          continue@colLoop
        }
      }

      if (cDiff == 0) {
        continue@colLoop
      }

      mirrorCol = c
      break@colLoop
    }

    return mirrorCol
  }

  private fun MutableDenseGrid<Char>.findHorizontalMirror(): Int {
    var mirrorRow = -1
    rowLoop@ for (r in 1..<rSize) {
      for (c in cIndices) {
        if (this[r, c] != this[r - 1, c]) {
          continue@rowLoop
        }
      }

      val rows = ((r - 2).downTo(0)).zip(r + 1..<rSize)
      val found = rows.all { (upperR, belowR) ->
        for (c in cIndices) {
          if (this[upperR, c] != this[belowR, c])
            return@all false
        }

        true
      }

      if (found) {
        mirrorRow = r
        break@rowLoop
      }
    }

    return mirrorRow
  }


  private fun MutableDenseGrid<Char>.findVerticalMirror(): Int {
    var mirrorCol = -1
    columnLoop@ for (c in 1..<cSize) {
      for (r in rIndices) {
        if (this[r, c] != this[r, c - 1]) {
          continue@columnLoop
        }
      }

      val columns = ((c - 2).downTo(0)).zip(c + 1..<cSize)
      val found = columns.all { (leftC, rightC) ->
        for (r in rIndices) {
          if (this[r, leftC] != this[r, rightC])
            return@all false
        }

        true
      }

      if (found) {
        mirrorCol = c
        break@columnLoop
      }
    }

    return mirrorCol
  }
}

object Parsing {
  val patternsP = gridP(chr('.').or(chr('#'))).manySep(newline).map { it.values }
}