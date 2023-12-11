package com.pbh.soft.common.grid

/*
 *     N
 *     |
 * W --+-- E
 *     |
 *     S
 */
enum class Dir(val dr: Int, val dc: Int) {
  NW(-1, -1), N(-1, 0), NE(-1, 1),
  W(0, -1), E(0, 1),
  SW(1, -1), S(1, 0), SE(1, 1);

  fun ray(
    from: Loc,
    minR: Row = Int.MIN_VALUE,
    maxR: Row = Int.MAX_VALUE,
    minC: Col = Int.MIN_VALUE,
    maxC: Col = Int.MAX_VALUE
  ): Sequence<Loc> = sequence {
    var loc = from + this@Dir
    while (loc.r in minR..maxR && loc.c in minC .. maxC) {
      yield(loc)
      loc += this@Dir
    }
  }

  companion object {
    val axisDirs = listOf(N, S, E, W)
    val verticalAxisDirs = listOf(N, S)
    val horizontalAxisDirs = listOf(E, W)

    fun Dir.opposite(): Dir = when (this) {
      NW -> SE
      N -> S
      NE -> SW
      W -> E
      E -> W
      SW -> NE
      S -> N
      SE -> NW
    }
  }
}