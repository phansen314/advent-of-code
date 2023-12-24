package com.pbh.soft.day15

import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day15.Op.Del
import com.pbh.soft.day15.Op.Ins
import com.pbh.soft.kparse.KParser
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.intNum
import com.pbh.soft.kparse.KParser.Companion.keepR
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.or
import com.pbh.soft.kparse.KParser.Companion.parser
import mu.KLogging

object Day15Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.problemP.onSuccess(text) { steps ->
    val answer = steps.sumOf { it.hash() }
    return "$answer"
  }

  override fun solveP2(text: String): String = Parsing.problemP.onSuccess(text) { steps ->
    val labelXboxId = mutableMapOf<Label, BoxId>(); steps.associate { it.label to it.label.hash() as BoxId }
    val boxIdXLenses = mutableMapOf<BoxId, ArrayList<Step>>()
    for (step in steps) {
      val boxId = labelXboxId.getOrPut(step.label) { step.label.hash() }
      val lenses = boxIdXLenses.getOrPut(boxId, ::ArrayList)
      when (step.op) {
        Del -> lenses.remove(step)
        is Ins -> {
          val idx = lenses.indexOf(step)
          if (idx == -1) lenses.add(step)
          else lenses[idx] = step
        }
      }
    }
    val answer = boxIdXLenses.asSequence()
      .map { (it.key + 1) * it.value.foldIndexed(0) { index, acc, step -> acc + (index + 1) * (step.op as Ins).focalLength } }
      .sum()



    return "$answer"
  }

  private fun Step.hash(): Int {
    val total = label.hash(0)
    return when (op) {
      Del -> '-'.hash(total)
      is Ins -> {
        op.focalLength.digitToChar().hash('='.hash(total))
      }
    }
  }

  private fun String.hash(input: Int = 0): Int {
    var total = input
    for (c: Int in chars()) {
      total += c
      total *= 17
      total %= 256
    }
    return total
  }

  private fun Char.hash(input: Int = 0): Int {
    var total = input + code
    total *= 17
    total %= 256
    return total
  }

  fun Step.pprint(): String = when (op) {
    Del -> "$label-"
    is Ins -> "$label=${op.focalLength}"
  }
}

typealias Label = String
typealias BoxId = Int

sealed class Op {
  data object Del : Op()
  data class Ins(val focalLength: Int) : Op()
}
class Step(val label: Label, val op: Op) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Step) return false
    return label == other.label
  }

  override fun hashCode(): Int {
    return label.hashCode()
  }

  override fun toString(): String = when (op) {
    Del -> "$label-"
    is Ins -> "$label=${op.focalLength}"
  }
}

object Parsing {
  val delOpP: KParser<Op> = chr('-').map { Del }
  val insOpP: KParser<Op> = chr('=').keepR(intNum).map { Ins(it) }
  val opP = delOpP.or(insOpP)
  val stepP = parser {
    val label = Regex("[^,=-]*")()
    val op = opP()
    Step(label, op)
  }
  val problemP = stepP.manySep(chr(',')).map { it.values }
}