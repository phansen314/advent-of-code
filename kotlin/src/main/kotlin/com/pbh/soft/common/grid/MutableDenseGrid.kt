package com.pbh.soft.common.grid

class MutableDenseGrid<T>(private val rows: ArrayList<ArrayList<T>>) : Iterable<Pair<Loc, T>> {
  val rSize: Int = rows.size
  val cSize: Int = rows[0].size
  val rIndices = 0..<rSize
  val cIndices = 0..<cSize

  companion object {
    operator fun <T> invoke(rSize: Int, cSize: Int, block: (Row, Col) -> T): MutableDenseGrid<T> {
      val rows = ArrayList<ArrayList<T>>(rSize)
      for (r in 0..<rSize) {
        val cols = ArrayList<T>(cSize)
        for (c in 0..<cSize) {
          cols.add(block(r, c))
        }
        rows.add(cols)
      }
      return MutableDenseGrid(rows)
    }
  }

  fun isInBounds(loc: Loc) = loc.r in 0..<rSize && loc.c in 0..<cSize

  override fun iterator(): Iterator<Pair<Loc, T>> = iterator {
    for (r in 0..<rSize) {
      for (c in 0..<cSize) {
        yield(Loc(r, c) to this@MutableDenseGrid[r, c]!!)
      }
    }
  }

  operator fun set(row: Row, pairs: Iterable<Pair<Col, T>>) {
    pairs.forEach { rows[row][it.first] = it.second }
  }

  operator fun set(row: Row, pairs: Sequence<Pair<Col, T>>) {
    pairs.forEach { rows[row][it.first] = it.second }
  }

  operator fun set(row: Row, col: Col, t: T) {
    rows[row][col] = t
  }

  operator fun set(loc: Loc, t: T) {
    set(loc.r, loc.c, t)
  }

  operator fun get(row: Row, col: Col): T = getOrNull(row, col) ?: throw IndexOutOfBoundsException("0 <= $row < $rSize and 0 <= $col < $cSize must hold true!")

  operator fun get(loc: Loc): T = get(loc.r, loc.c)

  fun getOrNull(row: Row, col: Col): T? =
    if (row in 0..<rSize && col in 0..<cSize) rows[row][col]
    else null

  fun getOrNull(loc: Loc): T? = get(loc.r, loc.c)

  inline fun findAll(crossinline predicate: (T) -> Boolean): Sequence<Pair<Loc, T>> =
    (0..<rSize).asSequence()
      .flatMap { r -> (0..<cSize)
        .filter { c -> predicate(get(r, c)!!) }
        .map { c -> Loc(r, c) to get(r, c)!! } }
}