package com.pbh.soft.day4

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.common.parsing.ParsingUtils.parseLines
import com.pbh.soft.day4.Parsing.cardP
import mu.KLogging

object Day4Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = cardP.parseLines(text).onSuccess { cards ->
    cards.asSequence()
      .map { 1 shl (it.numMatches() - 1) }
      .sum()
      .toString()
  }

  override fun solveP2(text: String): String = cardP.parseLines(text).onSuccess { cards ->
    val numMatches = cards.map { it.numMatches() }
    var numCopies = Array(numMatches.size) { 1 }
    for (i in numCopies.indices) {
      val n = numMatches[i]
      for (j in (i + 1)..(i + n)) {
        numCopies[j] += numCopies[i]
      }
    }
    numCopies.sum().toString()
  }

  private fun Card.numMatches(): Int =
    sequenceOf(winningNums, playerNums).flatMap { it }.groupingBy { it }.eachCount().asSequence()
      .filter { it.value > 1 }.count()
}

object Parsing {
  val intP = integer.map(Long::toInt)
  val numsP = chain(intP, whitespace1).map { it.terms }
  val cardP = parser {
    string("Card"); whitespace1();
    val id = intP();
    cc.ekblad.konbini.char(':')();
    whitespace1();
    val winningNums = numsP(); whitespace1(); cc.ekblad.konbini.char('|')(); whitespace1();
    val playerNums = numsP()
    Card(id, winningNums, playerNums)
  }
}

data class Card(val id: Int, val winningNums: List<Int>, val playerNums: List<Int>)