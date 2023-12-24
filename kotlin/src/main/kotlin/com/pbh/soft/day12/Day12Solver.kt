package com.pbh.soft.day12

import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.intNum
import com.pbh.soft.kparse.KParser.Companion.keepL
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.newline
import com.pbh.soft.kparse.KParser.Companion.opt
import com.pbh.soft.kparse.KParser.Companion.rgx
import com.pbh.soft.kparse.KParser.Companion.then
import com.pbh.soft.kparse.KParser.Companion.ws
import mu.KLogging

object Day12Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = Parsing.problemP.onSuccess(text) { problem ->
    println(problem)
    var total = 0L
    for (record in problem.records) {
      total += count(record.condition, record.groups)
    }

    total.toString()
  }

  override fun solveP2(text: String): String = Parsing.problemP.onSuccess(text) { problem ->
    println(problem)
    var total = 0L
    for ((i, record) in problem.records.withIndex()) {
      val condition = (0..<5).map { record.condition }.joinToString("?")
      val groups = (0..<5).flatMap { record.groups }
      total += count3(condition, groups)
    }

    total.toString()
  }

  private val cache = mutableMapOf<Pair<String, List<GroupSize>>, Long>()

  private fun count3(str: String, groups: List<GroupSize>): Long {
    return cache.getOrPut(str to groups) {
      val s = str.trimStart('.')
      if (groups.isEmpty()) return if ('#' !in str) 1 else 0
      if (str.isEmpty()) return 0

      val gsize = groups[0]
      var total = 0L
      for (i in s.indices) {
        if ((i + gsize <= s.length)
          && ((i..<i + gsize).all { s[it] != '.' })
          && (i == 0 || s[i - 1] != '#')
          && (i + gsize == s.length || s[i + gsize] != '#')
        ) {
          val nextS = if (i + gsize + 1 >= s.length) "" else s.substring(i + gsize + 1)
          total += count3(nextS, groups.drop(1))
        }
        if (s[i] == '#') break
      }
      total
    }
  }

  private fun count2(str: String, groups: List<GroupSize>): Long {
    return cache.getOrPut(str to groups) {
      val s = str.trimStart('.')
      if (s.isEmpty()) return if (groups.isEmpty()) 1 else 0
      if (groups.isEmpty()) return if ('#' !in s) 1 else 0
      if (s[0] == '#') {
        if (s.length < groups[0] || '.' in s.substring(0, groups[0])) return 0
        else if (s.length == groups[0]) return if (groups.size == 1) 1 else 0
        else if (s[groups[0]] == '#') return 0
        else return count2(s.substring(groups[0] + 1), groups.subList(1, groups.size))
      }
      val next = s.substring(1)
      count2("#$next", groups) + count2(next, groups)
    }
  }

  private fun count(s: String, groups: List<GroupSize>): Long {
    if (groups.isEmpty()) {
      return if ('#' !in s) 1 else 0
    }
    if (s.isEmpty()) return 0

    return cache.getOrPut(s to groups) {
      var total = 0L
      val gsize = groups[0]

      for (i in s.indices) {
        if (
        // check that group can exist in str
          i + gsize <= s.length &&
          // check if group size chars from si are not '.'
          (i..<(i + gsize)).all { s[it] != '.' } &&
          // check if it's a new group and not part of an existing group
          (i == 0 || s[i - 1] != '#') &&
          // last group making up the rest of s || end of current group isn't another '#'
          (i + gsize == s.length || s[i + gsize] != '#')
        ) {
          total += count(if (i + gsize + 1 >= s.length) "" else s.substring(i + gsize + 1), groups.drop(1))
        }

        if (s[i] == '#') break
      }

      return total
    }
  }
}

typealias GroupSize = Int

data class Record(val condition: String, val groups: List<GroupSize>)
data class Problem(val records: List<Record>)

object Parsing {
  val groupsP = intNum.manySep(chr(',')).map { it.values }
  val conditionP = rgx(Regex("[.#?]*"))
  val recordP = conditionP.keepL(ws).then(groupsP).map { (condition, groups) -> Record(condition, groups) }
  val problemP = recordP.manySep(newline.opt()).map { Problem(it.values) }
}