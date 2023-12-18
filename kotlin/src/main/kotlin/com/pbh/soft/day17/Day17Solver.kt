package com.pbh.soft.day17

import com.pbh.soft.common.ConsoleColors
import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Dir
import com.pbh.soft.common.grid.Dir.Companion.opposite
import com.pbh.soft.common.grid.Dir.E
import com.pbh.soft.common.grid.Dir.S
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.parsing.ParsingUtils
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.map
import mu.KLogging
import java.util.PriorityQueue

object Day17Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.blocksP.onSuccess(text) { blocks ->
    println(blocks.pprint())
    val goal = Loc(blocks.rSize - 1, blocks.cSize - 1)

    val frontier = PriorityQueue(compareBy(Crucible::cost)).apply {
      add(Crucible(blocks[0,1], Loc(0, 1), E, 1))
      add(Crucible(blocks[1,0], Loc(1, 0), S, 1))
    }
    val seen = mutableSetOf<Triple<Loc, Dir, MovesInDir>>()
    search@ while (frontier.isNotEmpty()) {
      val current = frontier.poll()
      if (current.cost % 10 == 0)
        logger.trace { "$current" }
      if (current.loc == goal) return@onSuccess "${current.cost}"

      dirs@ for (dir in Dir.axisDirs) {
        if ((dir == current.dir && current.movesInDir == 3) || dir == current.dir.opposite()) continue@dirs
        val nextLoc = current.loc + dir
        if (!blocks.isInBounds(nextLoc)) continue@dirs
        val successor = Crucible(
          cost = blocks[nextLoc] + current.cost,
          loc = nextLoc,
          dir = dir,
          movesInDir = if (dir == current.dir) current.movesInDir + 1 else 1
        )
        val seenSt = Triple(successor.loc, successor.dir, successor.movesInDir)
        if (seenSt in seen) continue@dirs
        seen.add(seenSt)
        frontier.add(successor)
      }
    }

    "shit"
  }

  override fun solveP2(text: String): String = Parsing.blocksP.onSuccess(text) { blocks ->
    println(blocks.pprint())
    val goal = Loc(blocks.rSize - 1, blocks.cSize - 1)

    val frontier = PriorityQueue(compareBy(UltraCrucible::cost)).apply {
      add(UltraCrucible(blocks[0,1], Loc(0, 1), E, 1))
      add(UltraCrucible(blocks[1,0], Loc(1, 0), S, 1))
    }
    val seen = mutableSetOf<Triple<Loc, Dir, MovesInDir>>()
    search@ while (frontier.isNotEmpty()) {
      val current = frontier.poll()
      if (current.cost % 10 == 0)
        logger.trace { "$current" }
      if (current.loc == goal) return@onSuccess "${current.cost}"

      dirs@ for (dir in Dir.axisDirs) {
        if ((dir == current.dir && current.movesInDir == 10) || dir == current.dir.opposite()) continue@dirs
        if (dir != current.dir && current.movesInDir < 4) continue@dirs
        val nextLoc = current.loc + dir
        if (!blocks.isInBounds(nextLoc)) continue@dirs
        val successor = UltraCrucible(
          cost = blocks[nextLoc] + current.cost,
          loc = nextLoc,
          dir = dir,
          movesInDir = if (dir == current.dir) current.movesInDir + 1 else 1
        )
        val seenSt = Triple(successor.loc, successor.dir, successor.movesInDir)
        if (seenSt in seen) continue@dirs
        seen.add(seenSt)
        frontier.add(successor)
      }
    }

    "shit"
  }


  private fun MutableDenseGrid<Int>.pprint(): String {
    val sb = StringBuilder()
    for (r in rIndices) {
      for (c in cIndices) {
        val heatLoss = this[r, c]
        val backgroundColor = if (heatLoss < 4) {
          ConsoleColors.RED_BACKGROUND
        } else if (heatLoss < 7) {
          ConsoleColors.YELLOW_BACKGROUND
        } else {
          ConsoleColors.CYAN_BACKGROUND
        }
        sb.append(backgroundColor)
        sb.append(heatLoss)
        sb.append(ConsoleColors.RESET)
      }
      sb.appendLine()
    }

    return sb.toString()
  }

}

typealias Cost = Int
typealias MovesInDir = Int

data class Crucible(val cost: Cost, val loc: Loc, val dir: Dir, val movesInDir: MovesInDir)
data class UltraCrucible(val cost: Cost, val loc: Loc, val dir: Dir, val movesInDir: MovesInDir)

object Parsing {
  val blocksP = ParsingUtils.gridP(KParser.digit.map(Char::digitToInt))
}