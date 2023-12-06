package com.pbh.soft.day6

import cc.ekblad.konbini.*
import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day6.Parsing.inputP
import com.pbh.soft.day6.Parsing.part2P
import mu.KLogging

object Day6Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = inputP.parse(text).onSuccess { races ->
    // -t^2 + 7t - 9 = 0
    // a=-1,b=49787980,c=-298118510661181


    val answer = races.asSequence()
      .map { race ->
        (0..race.time).asSequence()
          .map { t -> t * (race.time - t) }
          .filter { it > race.recordDistance }
          .count()
      }
      .fold(1) { a, b -> a * b }

    return answer.toString()
  }

  override fun solveP2(text: String): String = part2P.parse(text).onSuccess { (time, recordDistance) ->
    println(6961001L-42827000L)

    val answer = (0..time).asSequence()
      .map { t -> t * (time - t) }
      .filter { it > recordDistance }
      .count()

    return answer.toString()
  }
}

object Parsing {
  val intP = integer.map(Long::toInt)
  val inputP = parser {
    string("Time:"); whitespace1();
    val times = chain(intP, whitespace1).terms; whitespace1();
    string("Distance:"); whitespace1();
    val distances = chain(intP, whitespace1).terms;
    times.zip(distances).map { (t, d) -> Race(t, d) }
  }

  val part2P = parser {
    string("Time:"); whitespace1();
    val time = chain(cc.ekblad.konbini.regex("[0-9]+"), whitespace1).terms.joinToString("").toLong(); whitespace1();
    string("Distance:"); whitespace1();
    val distance = chain(cc.ekblad.konbini.regex("[0-9]+"), whitespace1).terms.joinToString("").toLong();
    Pair(time, distance)
  }
}

data class Race(val time: Int, val recordDistance: Int)