package com.pbh.soft.common.grid

data class Loc(val r: Int, val c: Int) {
  operator fun plus(dir: Dir) = Loc(r + dir.dr, c + dir.dc)
  operator fun plus(dir: Pair<Int, Int>) = Loc(r + dir.first, c + dir.second)

  fun neighbors() = Dir.entries.asSequence().map { this + it }
}