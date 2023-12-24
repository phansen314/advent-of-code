package com.pbh.soft.common.grid

import kotlin.math.abs

data class Loc(val r: Int, val c: Int) {
  operator fun plus(dir: Dir) = Loc(r + dir.dr, c + dir.dc)
  operator fun plus(dir: Pair<Int, Int>) = Loc(r + dir.first, c + dir.second)
  operator fun plus(other: Loc) = Loc(r + other.r, c + other.c)
  operator fun minus(other: Loc) = Loc(r - other.r, c - other.c)
  operator fun plus(n: Int): Loc = Loc(n * r, c * n)

  fun abs() = Loc(abs(r), abs(c))
  fun sum() = r + c
  fun neighbors() = Dir.entries.asSequence().map { this + it }

  companion object {
    fun manhattanDistance(locA: Loc, locB: Loc) = (locA - locB).abs().sum()
  }
}