package com.pbh.soft.common.grid

/*
 *     N
 *     |
 * W --+-- E
 *     |
 *     S
 */
enum class Dir(val dr: Int, val dc: Int, val bitIndex: Int) {
  NW(-1, -1, 0), N(-1, 0, 1), NE(-1, 1, 2),
  W(0, -1, 3), E(0, 1, 4),
  SW(1, -1, 5), S(1, 0, 6), SE(1, 1, 7);

  val bitMask: Int = 1 shl bitIndex
  val isVerticalAxis = dc == 0
  val isHorizontalAxis = dr == 0

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
    val axisDirs = linkedSetOf(N, S, E, W)
    val verticalAxisDirs = linkedSetOf(N, S)
    val horizontalAxisDirs = linkedSetOf(E, W)

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