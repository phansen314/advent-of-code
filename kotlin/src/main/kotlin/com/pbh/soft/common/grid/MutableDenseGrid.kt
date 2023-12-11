package com.pbh.soft.common.grid

class MutableDenseGrid<T>(private val rows: ArrayList<ArrayList<T>>) {
  val rSize: Int = rows.size
  val cSize: Int = rows[0].size

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

  operator fun get(row: Row, col: Col): T? =
    if (row in 0..<rSize && col in 0..<cSize) rows[row][col]
    else null

  operator fun get(loc: Loc): T? = get(loc.r, loc.c)

  inline fun findAll(crossinline predicate: (T) -> Boolean): Sequence<Pair<Loc, T>> =
    (0..<rSize).asSequence()
      .flatMap { r -> (0..<cSize)
        .filter { c -> predicate(get(r, c)!!) }
        .map { c -> Loc(r, c) to get(r, c)!! } }
}