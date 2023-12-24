package com.pbh.soft.day9

import com.pbh.soft.common.Solver
import com.pbh.soft.common.parsing.ParsingUtils.onSuccess
import com.pbh.soft.day9.Parsing.reportP
import com.pbh.soft.kparse.KParser.Companion.chr
import com.pbh.soft.kparse.KParser.Companion.intNum
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.newline
import mu.KLogging

object Day9Solver : Solver, KLogging() {
  override fun solveP1(text: String): String = reportP.onSuccess(text) { report ->
    var total = 0
    for (history in report.histories) {
      for (end in (history.size - 1).downTo(1)) {
        var allZero = true
        for (i in 0..<end) {
          history[i] = history[i+1] - history[i]
          allZero = allZero && history[i] == 0
        }
        history[history.size - 1] += history[end - 1]
        if (allZero) break
      }
      total += history[history.size - 1]
    }

    return total.toString()
  }

  override fun solveP2(text: String): String = reportP.onSuccess(text) { report ->
    var total = 0
    for (history in report.histories) {
      for (start in 1..<(history.size - 1)) {
        var allZero = true
        for (i in (history.size - 1).downTo(start)) {
          history[i] = history[i] - history[i-1]
          allZero = allZero && history[i] == 0
        }
        if (allZero) {
          total += (start - 1).downTo(0).fold(0) { acc, i ->
            history[i] - acc
          }
          break
        }
      }
    }

    total.toString()
  }
}

object Parsing {
  val historyP = intNum.manySep(chr(' ')).map { it.values.toMutableList() }
  val reportP = historyP.manySep(newline).map { Report(it.values) }
}

typealias History = MutableList<Int>
data class Report(val histories: List<History>)