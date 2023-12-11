package com.pbh.soft.day10

import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Col
import com.pbh.soft.common.grid.Dir
import com.pbh.soft.common.grid.Dir.*
import com.pbh.soft.common.grid.Dir.Companion.axisDirs
import com.pbh.soft.common.grid.Dir.Companion.opposite
import com.pbh.soft.common.grid.Dir.Companion.verticalAxisDirs
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.Row
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day10.Parsing.sketchP
import com.pbh.soft.day10.Pipe.*
import com.pbh.soft.day10.Tile.*
import com.pbh.soft.kparse.KParser.Companion.any
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.newline
import com.pbh.soft.kparse.KParser.Companion.parser
import com.pbh.soft.kparse.KParser.Companion.pos
import com.pbh.soft.kparse.KParser.Companion.then
import mu.KLogging
import kotlin.math.max

object Day10Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = sketchP.onSuccess(text) { sketch ->
    val startPipe = Pipe.entries.find { from ->
      val dirs = Dir.axisDirs.filter { via ->
        val to = sketch.pipes[sketch.start + via] ?: return@filter false
        from.canBeConnected(via, to)
      }
      dirs.size == 2
    }!!
    logger.debug { startPipe }

    var (expA, expB) = startPipe.dirs.map { Explorer(1, sketch.start + it, it) }
    while (expA.loc != expB.loc) {
      val (nextA, nextB) = expA.move(sketch.pipes) to expB.move(sketch.pipes)
      logger.debug { "expA $expA --> $nextA" }
      logger.debug { "expA $expB --> $nextB" }
      expA = nextA
      expB = nextB
    }

    logger.info { "expA=${expA.distance} expB=${expB.distance}" }

    return expA.distance.toString()
  }

  override fun solveP2(text: String): String = sketchP.onSuccess(text) { sketch ->
    logger.debug { sketch }
    val startPipe = Pipe.entries.find { from ->
      val dirs = Dir.axisDirs.filter { via ->
        val to = sketch.pipes[sketch.start + via] ?: return@filter false
        from.canBeConnected(via, to)
      }
      dirs.size == 2
    }!!
    logger.debug { startPipe }

    val pathLocs = mutableSetOf(sketch.start)
    var (expA, expB) = startPipe.dirs.map { Explorer(1, sketch.start + it, it) }
    while (expA.loc != expB.loc) {
      pathLocs.add(expA.loc)
      pathLocs.add(expB.loc)
      val (nextA, nextB) = expA.move(sketch.pipes) to expB.move(sketch.pipes)
      logger.trace { "expA $expA --> $nextA" }
      logger.trace { "expA $expB --> $nextB" }
      expA = nextA
      expB = nextB
    }
    pathLocs.add(expA.loc)
    logger.debug { "expA=${expA.distance} expB=${expB.distance}" }

    val innerPoints = mutableSetOf<Loc>()
    var area = 0
    for (r in 0..<sketch.maxR) {
      for (c in 0..<sketch.maxC) {
        val loc = Loc(r, c)
        if (loc in pathLocs) continue
        val cnt = W.ray(loc, 0, sketch.maxR, 0, sketch.maxC)
          .filter { it in pathLocs }
          .count { rloc ->
            sketch.pipes[rloc]?.let { it == V || it == Q2 || it == Q1 } ?: false
          }
        if (cnt % 2 == 1) {
          area += 1
          innerPoints.add(loc)
        }
      }
    }

    println(sketch.sketchString(pathLocs, innerPoints))

    return area.toString()
  }

  private fun Sketch.sketchString(pathLocs: Set<Loc>, innerPoints: Set<Loc>): String {
    var str = StringBuilder()
    for (r in 0..<maxR) {
      for (c in 0..<maxC) {
        when (val loc = Loc(r, c)) {
          in innerPoints -> str.append('I')
          start -> str.append('S')
          in pathLocs -> str.append('#')
          in groundLocs -> str.append('.')
          else -> str.append(pipes[loc]!!.char)
        }
      }
      str.appendLine()
    }

    return str.toString()
  }

  private fun Explorer.move(pipes: Map<Loc, Pipe>): Explorer {
    val outDir = pipes[loc]!!.oppositeDir(inDir.opposite())
    return Explorer(distance + 1, loc + outDir, outDir)
  }

  private fun Pipe.oppositeDir(dir: Dir): Dir = if (dirs[0] == dir) dirs[1] else dirs[0]

  private fun Pipe.canBeConnected(via: Dir, to: Pipe): Boolean = when (via) {
    N -> (this == V || this == Q1 || this == Q2) && (to == V || to == Q3 || to == Q4)
    S -> (this == V || this == Q3 || this == Q4) && (to == V || to == Q1 || to == Q2)
    E -> (this == H || this == Q1 || this == Q4) && (to == H || to == Q2 || to == Q3)
    W -> (this == H || this == Q2 || this == Q3) && (to == H || to == Q1 || to == Q4)
    else -> false
  }

}

/*
 *     N
 *     |
 * W --+-- E
 *     |
 *     S
 */

data class Explorer(val distance: Int, val loc: Loc, val inDir: Dir)

data class Sketch(
  val start: Loc,
  val maxR: Row,
  val maxC: Col,
  val pipes: Map<Loc, Pipe>,
  val groundLocs: MutableList<Loc>
)

enum class Pipe(val char: Char, val dirs: List<Dir>) {
  V('|', listOf(N, S)),
  H('-', listOf(W, E)),
  Q1('L', listOf(N, E)),
  Q2('J', listOf(N, W)),
  Q3('7', listOf(S, W)),
  Q4('F', listOf(S, E)),
}

sealed class Tile {
  abstract val loc: Loc

  data class Ground(override val loc: Loc) : Tile()
  data class Start(override val loc: Loc) : Tile()
  data class PipeTile(override val loc: Loc, val pipe: Pipe) : Tile()
}

object Parsing {
  private val locP = pos.map { Loc(r = it.line, c = it.column) }
  val pipeP = locP.then(chr(Pipe.entries.associateBy(Pipe::char))).map { (loc, pipe) -> PipeTile(loc, pipe) }
  val startP = locP.then(chr('S')).map { (loc, _) -> Start(loc) }
  val groundP = locP.then(chr('.')).map { (loc, _) -> Ground(loc) }
  val tileP = any<Tile>(groundP, startP, pipeP)
  val sketchP = parser {
    var maxR: Row = 0
    var maxC: Col = -1
    var start = Loc(-1, -1)
    val pipes = mutableMapOf<Loc, Pipe>()
    val groundLocs = mutableListOf<Loc>()
    while (isNotDone()) {
      for ((c, tile) in many(tileP).withIndex()) {
        when (tile) {
          is Ground -> groundLocs.add(tile.loc)
          is PipeTile -> pipes[tile.loc] = tile.pipe
          is Start -> start = tile.loc
        }
        maxC = max(maxC, c)
      }
      opt(newline)
      maxR++
    }
    Sketch(start, maxR, maxC + 1, pipes, groundLocs)
  }
}