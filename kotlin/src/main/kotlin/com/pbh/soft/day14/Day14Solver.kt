package com.pbh.soft.day14

import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.parsing.ParsingUtils
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day14.Cell.Companion.chrMap
import com.pbh.soft.day14.Cell.Empty
import com.pbh.soft.day14.Cell.RoundBoulder
import com.pbh.soft.day14.Day14Solver.pprint
import com.pbh.soft.kparse.KParser.Companion.chr
import mu.KLogging

object Day14Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.gridP.onSuccess(text) { grid ->


    var total = 0
    val states = grid.cIndices.mapTo(mutableListOf()) { State() }
    for (r in grid.rIndices) {
      for (c in grid.cIndices) {
        val currentSt = states[c]
        val nextSt = when (grid[r, c]) {
          RoundBoulder -> State(currentSt.current, currentSt.numBoulders + 1)
          Cell.SquareBoulder -> {
            // add currentSt total
            total += sumN(grid.rSize - currentSt.current) - sumN(grid.rSize - currentSt.current - currentSt.numBoulders)
            State(r + 1, 0)
          }

          Empty -> currentSt
        }
        states[c] = nextSt
      }
    }
    for (st in states) {
      total += sumN(grid.rSize - st.current) - sumN(grid.rSize - st.current - st.numBoulders)
    }



    total.toString()
  }

  override fun solveP2(text: String): String = Parsing.gridP.onSuccess(text) { grid ->
//    println(grid.pprint(Cell::char))
//    println("---")
    //NORTH
    val northTilt = Tilt(1..<grid.rSize, -1, grid.cIndices, 0)
    // SOUTH
    val southTilt = Tilt((grid.rSize - 2).downTo(0), 1, grid.cIndices, 0)
    // EAST
    val eastTilt = Tilt(grid.rIndices, 0, ((grid.cSize - 2).downTo(0)), 1)
    // WEST
    val westTilt = Tilt(grid.rIndices, 0, 1..<grid.cSize, -1)

    val cache = mutableMapOf<String, Int>()
    var i = 1
    while (i < 1000000000) {
      for (tilt in listOf(northTilt, westTilt, southTilt, eastTilt)) {
        grid.tilt(tilt)
      }
      val s = grid.pprint(Cell::char)
      if (cache.contains(s)) {
        println("i: $i")
        val di = i - cache[s]!!
        val j = (999999999 - i + 1) % di
        for (_a in 0..<j) {
          for (tilt in listOf(northTilt, westTilt, southTilt, eastTilt)) {
            grid.tilt(tilt)
          }
        }

        var total = 0
        val states = grid.cIndices.mapTo(mutableListOf()) { State() }
        for (r in grid.rIndices) {
          for (c in grid.cIndices) {
            if (grid[r, c] == RoundBoulder) {
              total += grid.rSize - r
            }
          }
        }

        return@onSuccess total.toString()

      }
      cache[s] = i
      i++
    }

    ""
  }

  private fun <T> MutableDenseGrid<T>.pprint(transform: (T) -> Char): String {
    val sb = StringBuilder()
    for (r in rIndices) {
      for (c in cIndices) {
        sb.append(transform(this[r, c]))
      }
      sb.appendLine()
    }
    return sb.toString()
  }

  fun sumN(n: Int) = (n * (n + 1)) / 2

  fun MutableDenseGrid<Cell>.tilt(tilt: Tilt) {
    val (rows, dr, cols, dc) = tilt
    var isMoved: Boolean
    do {
      isMoved = false
      for (r in rows) {
        for (c in cols) {
          if (this[r + dr, c + dc] == Empty && this[r, c] == RoundBoulder) {
            this[r + dr, c + dc] = RoundBoulder
            this[r, c] = Empty
            isMoved = true
          }
        }
      }
    } while (isMoved)
  }
}

data class Tilt(val rows: IntProgression, val dr: Int, val cols: IntProgression, val dc: Int)

data class State(val current: Int = 0, val numBoulders: Int = 0)

enum class Cell(val char: Char) {
  RoundBoulder('O'),
  SquareBoulder('#'),
  Empty('.');

  companion object {
    val chrMap = Cell.entries.associateBy(Cell::char)
  }
}

object Parsing {
  val gridP = ParsingUtils.gridP(chr(chrMap))
}