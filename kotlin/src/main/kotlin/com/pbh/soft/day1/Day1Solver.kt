package com.pbh.soft.day1

import com.pbh.soft.common.Solver
import mu.KLogging

object Day1Solver : Solver, KLogging() {
    val regex = Regex("(?=(\\d|one|two|three|four|five|six|seven|eight|nine))")

    override fun solveP1(text: String): String {
        return text.lines().asSequence()
            .map { line ->
                var (first, last) = 0 to 0
                for (i in line.indices) {
                    if (line[i].isDigit()) {
                        first = i
                        break
                    }
                }
                for (i in line.indices.reversed()) {
                    if (line[i].isDigit()) {
                        last = i
                        break
                    }
                }
                line[first].digitToIntOrNull()!! * 10 + line[last].digitToIntOrNull()!!
            }
            .sum()
            .toString()
    }

    override fun solveP2(text: String): String {
        return text.lines().asSequence()
            .map { line ->
                val all = regex.findAll(line).toList()
                val x = all.first().groupValues[1].toInt() * 10 + all.last().groupValues[1].toInt()
                logger.debug { "$x - $line" }
                x
            }
            .sum()
            .toString()
    }

    private val lookup = run {
        val map = mutableMapOf<String, Int>()
        (1..9).forEach { map[it.toString()] = it }
        listOf("one", "two", "three", "four", "five", "six", "seven", "eight", "nine").forEachIndexed { i, s -> map[s] = i + 1 }
        map.toMap()
    }

    private fun String.toInt(): Int = lookup[this] ?: throw IllegalStateException("wtf")
}