package com.pbh.soft.day2

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.common.parsing.ParsingUtils.parseLines
import com.pbh.soft.day2.Color.*
import com.pbh.soft.day2.Parsing.gameP
import mu.KLogging
import kotlin.math.max

object Day2Solver : Solver, KLogging() {
  val bag = mkBag(12, 13, 14)

  override fun solveP1(text: String): String =
    gameP.parseLines(text).onSuccess { games ->
      games.asSequence()
        .filter { it.isPossible(bag) }
        .map(Game::id)
        .sum()
        .toString()
    }

  override fun solveP2(text: String): String =
    gameP.parseLines(text).onSuccess { games ->
      games.asSequence()
        .map { it.maxColors().power() }
        .sum()
        .toString()
    }

  private fun mkBag(r: Int, g: Int, b: Int): Bag = Bag(linkedMapOf(R to r, G to g, B to b))

  private fun Grabs.isPossible(bag: Bag): Boolean = Color.entries.all { bag[it] >= this[it] }

  private fun Game.isPossible(bag: Bag): Boolean = grabs.all { grab -> grab.isPossible(bag) }

  private fun Game.maxColors(): ColorsMap {
    val maxMap = Color.entries.associateWithTo(mutableMapOf()) { -1 }
    for (grab in grabs) {
      for (color in Color.entries) {
        maxMap.merge(color, grab[color], ::max)
      }
    }
    return ColorsMap(maxMap)
  }

  private fun ColorsMap.power(): Int = map.values.fold(1) { acc, cv -> acc * cv }
}

object Parsing {
  val intP: Parser<Int> = integer.map(Long::toInt)

  val colorP: Parser<Color> = oneOf(
    string(R.text).map { R },
    string(G.text).map { G },
    string(B.text).map { B })

  val colorAmountP: Parser<Pair<Color, Int>> = parser {
    whitespace()
    val amount = intP()
    whitespace1()
    val color = colorP()
    color to amount
  }

  val grabsP: Parser<Grabs> = chain(colorAmountP, char(',')).map { Grabs(it.terms.toMap(LinkedHashMap())) }
  val gameP: Parser<Game> = parser {
    string("Game ");
    val id = intP(); string(":")
    val grabs = chain1(grabsP, cc.ekblad.konbini.char(';')).terms
    Game(id, grabs)
  }
}

enum class Color(val text: String) {
  R("red"),
  G("green"),
  B("blue");
}

data class ColorsMap(val map: Map<Color, Int>) {
  operator fun get(color: Color): Int = map[color] ?: 0
}

typealias Bag = ColorsMap
typealias Grabs = ColorsMap

data class Game(val id: Int, val grabs: List<Grabs>)