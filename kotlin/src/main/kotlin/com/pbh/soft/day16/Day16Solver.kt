package com.pbh.soft.day16

import com.pbh.soft.common.ConsoleColors
import com.pbh.soft.common.ConsoleColors.BLACK
import com.pbh.soft.common.ConsoleColors.BLACK_BACKGROUND
import com.pbh.soft.common.ConsoleColors.GREEN_BACKGROUND
import com.pbh.soft.common.ConsoleColors.RESET
import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Dir
import com.pbh.soft.common.grid.Dir.*
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.parsing.ParsingUtils
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day16.Day16Solver.pprint
import com.pbh.soft.day16.Optic.*
import com.pbh.soft.day16.Parsing.layoutP
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.or
import mu.KLogging
import java.util.*
import kotlin.math.max

object Day16Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = layoutP.onSuccess(text) { grid ->
    val beams = LinkedList<Beam>().apply { add(Beam.initial) }
    val energized = mutableSetOf<Loc>()
    var done: Boolean
    simulation@ do {
      done = true
      val created = LinkedList<Beam>()
      val deleted = LinkedList<Beam>()
      beams@ for (beam in beams) {
        val nextLoc = beam.nextLoc()
        if (!grid.isInBounds(nextLoc)) {
          deleted.add(beam)
          continue@beams
        }
        val nextCell = grid[nextLoc]
        if (nextCell.seenDir(beam.dir)) {
          deleted.add(beam)
          continue@beams
        }

        done = false
        nextCell.addDir(beam.dir)
        energized.add(nextLoc)
        beam.loc = nextLoc

        when (grid[nextLoc].optic) {
          null -> {}
          MIRROR_45 -> {
            beam.dir = when (beam.dir) {
              N -> E
              W -> S
              E -> N
              else -> W
            }
          }

          MIRROR_135 -> {
            beam.dir = when (beam.dir) {
              N -> W
              W -> N
              E -> S
              else -> E
            }
          }

          VERTICAL_SPLITTER -> {
            if (beam.dir.isHorizontalAxis) {
              beam.dir = N
              created.add(Beam(beam.loc, S))
            }
          }

          HORIZONTAL_SPLITTER -> {
            if (beam.dir.isVerticalAxis) {
              beam.dir = W
              created.add(Beam(beam.loc, E))
            }
          }
        }
      }
      beams.removeAll(deleted)
      beams.addAll(created)
    } while (!done)
    println(grid.pprint(energized))

    val answer = energized.size
    "$answer"
  }

  override fun solveP2(text: String): String = layoutP.onSuccess(text) { grid ->
    val initialBeams = sequence {
      for (r in grid.rIndices) {
        yield(Beam(Loc(r, -1), E))
        yield(Beam(Loc(r, grid.cSize), W))
      }
      for (c in grid.cIndices) {
        yield(Beam(Loc(-1, c), S))
        yield(Beam(Loc(grid.rSize, c), N))
      }
    }
    var max = 0
    for (initialBeam in initialBeams) {
      for (r in grid.rIndices) {
        for (c in grid.cIndices) {
          grid[r, c].incomingDirBits = 0
        }
      }
      val beams = LinkedList<Beam>().apply { add(initialBeam) }
      val energized = mutableSetOf<Loc>()
      var done: Boolean
      simulation@ do {
        done = true
        val created = LinkedList<Beam>()
        val deleted = LinkedList<Beam>()
        beams@ for (beam in beams) {
          val nextLoc = beam.nextLoc()
          if (!grid.isInBounds(nextLoc)) {
            deleted.add(beam)
            continue@beams
          }
          val nextCell = grid[nextLoc]
          if (nextCell.seenDir(beam.dir)) {
            deleted.add(beam)
            continue@beams
          }

          done = false
          nextCell.addDir(beam.dir)
          energized.add(nextLoc)
          beam.loc = nextLoc

          when (grid[nextLoc].optic) {
            null -> {}
            MIRROR_45 -> {
              beam.dir = when (beam.dir) {
                N -> E
                W -> S
                E -> N
                else -> W
              }
            }

            MIRROR_135 -> {
              beam.dir = when (beam.dir) {
                N -> W
                W -> N
                E -> S
                else -> E
              }
            }

            VERTICAL_SPLITTER -> {
              if (beam.dir.isHorizontalAxis) {
                beam.dir = N
                created.add(Beam(beam.loc, S))
              }
            }

            HORIZONTAL_SPLITTER -> {
              if (beam.dir.isVerticalAxis) {
                beam.dir = W
                created.add(Beam(beam.loc, E))
              }
            }
          }
        }
        beams.removeAll(deleted)
        beams.addAll(created)
      } while (!done)
//      println(grid.pprint(energized))
//      println("${energized.size}${"-".repeat(80)}")
      max = max(max, energized.size)
    }

    "$max"
  }


  private fun MutableDenseGrid<Cell>.pprint(energized: Set<Loc>): String {
    val sb = StringBuilder()
    for (r in rIndices) {
      for (c in cIndices) {
        val backgroundColor = if (Loc(r, c) in energized) GREEN_BACKGROUND else BLACK_BACKGROUND
        when (val optic = this[r, c].optic) {
          null -> sb.run {
            append(ConsoleColors.WHITE)
            append(backgroundColor)
            append(".")
          }

          else -> {
            sb.run {
              append(BLACK)
              append(backgroundColor)
              append("${optic.char}")
            }
          }
        }
        sb.append(RESET)
      }
      sb.appendLine()
    }

    return sb.toString()
  }

}

class Beam private constructor(val id: Int, var loc: Loc, var dir: Dir) {
  companion object {
    private var id = 0
    operator fun invoke(loc: Loc, dir: Dir) = Beam(id++, loc, dir)

    val initial = Beam(Loc(0, -1), E)
  }

  fun nextLoc() = loc + dir

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Beam) return false
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun toString(): String = "Beam($id, (${loc.r}, ${loc.c}), $dir)"
}

enum class Optic(val char: Char) {
  MIRROR_45('/'),
  MIRROR_135('\\'),
  VERTICAL_SPLITTER('|'),
  HORIZONTAL_SPLITTER('-');

  companion object {
    val charMap = Optic.entries.associateBy(Optic::char)
  }
}

data class Cell(val optic: Optic? = null, var incomingDirBits: InDirBits = 0) {
  fun seenDir(d: Dir) = (incomingDirBits and d.bitMask) > 0
  fun addDir(d: Dir) {
    incomingDirBits = incomingDirBits or d.bitMask
  }

  override fun toString(): String {
    val char = when (optic) {
      null -> '.'
      MIRROR_45 -> '/'
      MIRROR_135 -> '\\'
      VERTICAL_SPLITTER -> '|'
      HORIZONTAL_SPLITTER -> '-'
    }
    val bits = Integer.toBinaryString(incomingDirBits).padStart(10, '0')
    return "Cell($char, $bits)"
  }
}

typealias InDirBits = Int

object Parsing {
  val opticP: KParser<Optic?> = chr('.').map { null as Optic? }.or(chr(Optic.charMap))
  val layoutP = ParsingUtils.gridP(opticP.map(::Cell))
}