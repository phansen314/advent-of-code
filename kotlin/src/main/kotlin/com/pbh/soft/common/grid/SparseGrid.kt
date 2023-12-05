package com.pbh.soft.common.grid

class SparseGrid<T>(val rows: Map<Row, Map<Col, T>>) : Map<Row, Map<Col, T>> by rows {
  companion object {
    operator fun <T> invoke(block: Dsl<T>.() -> Unit) = Dsl<T>().apply(block).build()
  }

  operator fun get(row: Row, col: Col): T? = rows[row]?.let { it[col] }
  operator fun get(loc: Loc): T? = get(loc.r, loc.c)

  inline fun findAll(crossinline predicate: (T) -> Boolean): Sequence<Pair<Loc, T>> =
    entries.asSequence().flatMap { (r, colmap) ->
      colmap.entries.asSequence()
        .filter { (c, t) -> predicate(t) }
        .map { (c, t) -> Loc(r, c) to t }
    }

  inline fun findAllByRow(crossinline predicate: (T) -> Boolean): Sequence<Pair<Row, Sequence<Pair<Loc, T>>>> =
    entries.asSequence().map { (r, colmap) ->
      r to colmap.entries.asSequence()
        .filter { (c, t) -> predicate(t) }
        .map { (c, t) -> Loc(r, c) to t }
    }

  inline fun <reified U : T> entriesByType(): Sequence<Pair<Loc, U>> {
    @Suppress("UNCHECKED_CAST")
    return asSequence().flatMap { (r, colmap) ->
      colmap.asSequence().filter { it.value is U }.map { (c, u) -> Loc(r, c) to u }
    } as Sequence<Pair<Loc, U>>
  }

  inline fun <reified U : T> entriesByRowByType(): Sequence<Pair<Row, List<Pair<Col, U>>>> {
    @Suppress("UNCHECKED_CAST")
    return asSequence().map { (r, colmap) ->
      r to colmap.asSequence().filter { it.value is U }.map { (c, u) -> c to u }.toList()
    } as Sequence<Pair<Row, List<Pair<Col, U>>>>
  }

  class Dsl<T> {
    private val rows = mutableMapOf<Row, MutableMap<Col, T>>()
    val grid = this

    fun build() = SparseGrid(rows.toMap())

    operator fun set(row: Row, pairs: Iterable<Pair<Col, T>>) {
      rows[row] = pairs.toMap(mutableMapOf())
    }

    operator fun set(row: Row, pairs: Sequence<Pair<Col, T>>) {
      rows[row] = pairs.toMap(mutableMapOf())
    }

    operator fun set(row: Row, col: Col, t: T) {
      rows.getOrPut(row, ::mutableMapOf)[col] = t
    }

    operator fun set(loc: Loc, t: T) {
      set(loc.r, loc.c, t)
    }
  }
}