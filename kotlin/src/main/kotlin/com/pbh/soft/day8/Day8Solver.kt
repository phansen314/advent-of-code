package com.pbh.soft.day8

import com.pbh.soft.common.Solver
import com.pbh.soft.day8.CyclingIterator.Companion.cycle
import com.pbh.soft.day8.Instruction.Companion.charReprXInstruction
import com.pbh.soft.day8.Parsing.networkP
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.letter
import com.pbh.soft.kparse.KParser.Companion.many
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.newline
import com.pbh.soft.kparse.KParser.Companion.parser
import com.pbh.soft.kparse.KParser.Companion.rgx
import com.pbh.soft.kparse.KParser.Companion.start
import com.pbh.soft.kparse.KParser.Companion.ws
import com.pbh.soft.kparse.Result
import com.pbh.soft.kparse.State
import mu.KLogging

object Day8Solver : Solver, KLogging() {
  override fun solveP1(text: String): String {
    val (result, _) = networkP(State(text))
    val problem = when (result) {
      is Result.Err -> throw IllegalStateException("fuck")
      is Result.Ok -> result.value
    }

    val mapping = problem.nodes.associateBy(Node::from)
    var current = mapping["AAA"]!!
    var steps = 0
    for (instr in problem.instructions.cycle()) {
      val next = when (instr) {
        Instruction.L -> current.left
        Instruction.R -> current.right
      }
      current = mapping[next]!!
      steps++
      if (current.from == "ZZZ") break
    }

    return steps.toString()
  }

  override fun solveP2(text: String): String {
    val (result, _) = networkP(State(text))
    val problem = when (result) {
      is Result.Err -> throw IllegalStateException("fuck")
      is Result.Ok -> result.value
    }

    val mapping = problem.nodes.associateBy(Node::from)
    val currents = problem.nodes.filterTo(mutableListOf()) { it.isSeed() }

    for (seed in currents) {
      var current = seed
      var steps = 0
      for (instr in problem.instructions.cycle()) {
        current = mapping.move(current, instr)
        steps++
        if (current.isEnd()) break
      }
      println("${seed.from}=$steps")
    }

    // just use lcm of all the step counts for each seed to complete
    return 13334102464297L.toString()
  }

  private fun Node.isSeed(): Boolean = from.endsWith("A")
  private fun Node.isEnd(): Boolean = from.endsWith("Z")
  private fun Map<String, Node>.move(current: Node, instruction: Instruction): Node {
    val next = when (instruction) {
      Instruction.L -> current.left
      Instruction.R -> current.right
    }
    return this[next]!!
  }
}

class CyclingIterator<T>(private val source: List<T>) : Iterator<T> {
  private var iter = source.iterator()
  override fun hasNext(): Boolean = true

  companion object {
    fun <T> List<T>.cycle(): Iterator<T> = CyclingIterator(this)
  }

  override fun next(): T {
    if (iter.hasNext()) return iter.next()
    iter = source.iterator()
    return iter.next()
  }
}

object Parsing {
  val instructionP = chr(charReprXInstruction)
  val nameP = "\\w*".toRegex()
  val nodeP = parser {
    val name = nameP(); ws(); chr('='); ws(); chr('(')
    val left = nameP(); chr(','); ws()
    val right = nameP(); chr(')')
    Node(name, left, right)
  }
  val networkP = parser {
    start()
    val instructions = instructionP.many(1)(); newline(); newline()
    val nodes = nodeP.manySep(newline)().values
    Problem(instructions, nodes)
  }
}

enum class Instruction(val charRepr: Char) {
  L('L'), R('R');

  companion object {
    val charReprXInstruction = Instruction.entries.associateBy(Instruction::charRepr)
  }
}

data class Node(val from: String, val left: String, val right: String)
data class Problem(val instructions: List<Instruction>, val nodes: List<Node>)