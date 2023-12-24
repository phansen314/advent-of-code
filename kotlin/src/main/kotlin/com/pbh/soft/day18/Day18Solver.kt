package com.pbh.soft.day18

import com.pbh.soft.common.ConsoleColors
import com.pbh.soft.common.Solver
import com.pbh.soft.common.grid.Dir
import com.pbh.soft.common.grid.Dir.*
import com.pbh.soft.common.grid.Loc
import com.pbh.soft.common.grid.MutableDenseGrid
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day18.Cell.*
import com.pbh.soft.day18.DigPlan.Companion.charDirMap
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.hexNum
import com.pbh.soft.kparse.KParser.Companion.intNum
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.newline
import com.pbh.soft.kparse.KParser.Companion.parser
import com.pbh.soft.kparse.KParser.Companion.rgx
import com.pbh.soft.kparse.KParser.Companion.then
import mu.KLogging
import java.io.File
import java.math.BigDecimal
import java.util.*
import kotlin.math.abs
import kotlin.math.max

object Day18Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.digPlanP.onSuccess(text) { plan ->
    val dirXlengths = Dir.entries.associateWithTo(mutableMapOf()) { 0 }
    plan.instructions.forEach { instruction -> dirXlengths.computeIfPresent(instruction.dir) { _, acc -> acc + instruction.length } }
    val (maxR, maxC) = max(dirXlengths[S]!!, dirXlengths[N]!!) to max(dirXlengths[W]!!, dirXlengths[E]!!)
    val cells = MutableDenseGrid<Cell>(2 * maxR + 1, 2 * maxC + 1) { _, _ -> Ground }
    val initial = Loc(maxR, maxC)
    var digger = initial
    cells[digger] = Trench
    for (instruction in plan.instructions) {
      for (l in 0..<instruction.length) {
        digger += instruction.dir
        cells[digger] = Trench
      }
    }

    val outside = Loc(0, 0)
    val frontier = Stack<Loc>().apply { add(outside) }
    var count = 0
    while (frontier.isNotEmpty()) {
      val current = frontier.pop()
      if (cells[current] != Fill) {
        cells[current] = Fill
        count++
      }

      dirs@ for (dir in Dir.entries) {
        val next = current + dir
        if (!cells.isInBounds(next) || cells[next] != Ground) continue@dirs
        frontier.push(next)
      }
    }

    println(cells.pprint())
    val answer = (cells.rSize * cells.cSize) - count

    "$answer"
  }

  override fun solveP2(text: String): String = Parsing.digPlanP2.onSuccess(text) { plan ->
    val x = Day18.part2()
    val points = plan.instructions.scan(Loc(0, 0)) { current, inst -> current + inst.dir * inst.length }
    val perim = plan.instructions.sumOf { it.length }
    val area = points.zipWithNext { a, b ->
      (a.c * b.r).toLong() - (a.r * b.c).toLong()
    }.sum() / 2

//    var sum = BigDecimal.ZERO
//    for (i in 0..<(points.size - 1)) {
//      val (rA, cA) = points[i]
//      val (rB, cB) = points[i + 1]
//      sum += BigDecimal((rA + rB) * (cA - cB))
//    }



    val answer = perim / 2L + abs(area) + 1

    "$answer"
  }

  private fun MutableDenseGrid<Cell>.pprint(): String {
    val sb = StringBuilder()
    for (r in rIndices) {
      for (c in cIndices) {
        val (backgroundColor, char) = when (this[r, c]) {
          Trench -> ConsoleColors.WHITE_BACKGROUND to '#'
          Ground -> ConsoleColors.BLACK_BACKGROUND to '.'
          Fill -> ConsoleColors.BLUE_BACKGROUND to '*'
        }
        sb.append(backgroundColor)
        sb.append(char)
        sb.append(ConsoleColors.RESET)
      }
      sb.appendLine()
    }

    return sb.toString()
  }

}

sealed class Cell {
  data object Trench : Cell()
  data object Ground : Cell()
  data object Fill : Cell()
}

typealias HexColor = Int

data class Instruction(val dir: Dir, val length: Int, val color: HexColor)
data class DigPlan(val instructions: List<Instruction>) {
  companion object {
    val charDirMap = mapOf('R' to E, 'D' to S, 'L' to W, 'U' to N)
  }
}

object Parsing {
  val instructionP = parser {
    val dir = chr(charDirMap); chr(' ')
    val length = intNum(); chr(' ')
    val color = bracket("(#", hexNum, ")")
    Instruction(dir, length, color)
  }
  val digPlanP = instructionP.manySep(newline).map { DigPlan(it.values) }

  val hex5 = rgx(Regex("${KParser.Nums.Hex.charRgx}{5}")).map { it.toInt(16) }
  val dirMap = mapOf('0' to E, '1' to S, '2' to W, '3' to N)
  val instructionP2 = parser {
    chr(charDirMap); chr(' '); intNum(); chr(' ')
    val (length, dir) = bracket("(#", hex5.then(KParser.chr(dirMap)), ")")
    Instruction(dir, length, 0)
  }
  val digPlanP2 = instructionP2.manySep(newline).map { DigPlan(it.values) }
}


object Day18 {
  private val inputs = File("C:\\Users\\superuser\\code\\advent-of-code\\days\\day18\\part2\\problem.in")
    .readLines()
    .map { line ->
      val split = line.split(' ')
      Triple(split[0][0], split[1].toLong(), split[2].removeSurrounding("(#", ")"))
    }

  private data class Vector2(val x: Long, val y: Long) {
    fun add(other: Vector2) = Vector2(x + other.x, y + other.y)

    fun multiply(multiplier: Long) = Vector2(x * multiplier, y * multiplier)
  }

  private val normalDigPlan = inputs.map { triple -> shiftDirection(triple.first).multiply(triple.second) }

  private val colorsDigPlan = inputs.map { triple ->
    shiftDirection(triple.third.last()).multiply(triple.third.dropLast(1).toLong(radix = 16))
  }

  private fun shiftDirection(direction: Char) =
    when (direction) {
      '0', 'R' -> Vector2(1, 0)
      '1', 'D' -> Vector2(0, 1)
      '2', 'L' -> Vector2(-1, 0)
      '3', 'U' -> Vector2(0, -1)
      else -> throw Exception("Unknown direction")
    }

  private fun cornerPositions(digPlan: List<Vector2>): List<Vector2> =
    digPlan.scan(Vector2(0, 0)) { acc, shiftVector ->
      acc.add(shiftVector)
    }

  private fun perimeter(digPlan: List<Vector2>): Long =
    digPlan.sumOf { abs(it.x) + abs(it.y) }

  private fun shoelaceFormula(corners: List<Vector2>): Long =
    corners.zipWithNext { a, b ->
      a.x * b.y - a.y * b.x
    }.sum() / 2

  private fun integerPointsFromPickTheorem(area: Long, perimeter: Long): Long =
    area + perimeter / 2L + 1L

  private fun lavaCapacity(digPlan: List<Vector2>): Long
    = integerPointsFromPickTheorem(shoelaceFormula(cornerPositions(digPlan)), perimeter(digPlan))

  fun part1() = println(lavaCapacity(normalDigPlan))

  fun part2() = lavaCapacity(colorsDigPlan)
}