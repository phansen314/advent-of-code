//package com.pbh.soft.kparse2
//
//
//
//
//val failP = KParser<Nothing> { throw ErrException(Result.Err(-100)) }
//val chrP = KParser<Char> { state ->
//  if (state.index >= state.input.length) Result.Err(-200) to state
//  else Result.Ok(state.input[state.index]) to state.copy(index = state.index + 1)
//}
//fun <T> many(p: KParser<T>) = parser {
//  val list = mutableListOf<T>()
//  while (true) {
//    try {
//      list.add(p())
//    } catch (_: ErrException) {
//      break
//    }
//  }
//  list.toList()
//}
//
//val eofP = KParser<Unit> { st ->
//  if (st.index == st.input.length) Result.Ok(Unit) to st
//  else Result.Err(-300) to st
//}
//
//fun main() {
//
//  val x = parser {
////    val a: Char = chrP()
////    val b: Char = chrP()
//////    failP()
////    a to b
//    many(chrP)()
//    eofP()
//  }
//  val (res, rest) = x(State("abc"))
//  when (res) {
//    is Result.Err -> println("wtf $res")
//    is Result.Ok -> println("yes $res")
//  }
//  println(rest)
//}
//
//sealed class Result<out T> {
//  data class Ok<T>(val value: T) : Result<T>()
//  data class Err(val x: Int) : Result<Nothing>()
//}
//
//fun interface KParser<T> : (State) -> Pair<Result<T>, State> {
//  fun parse(state: State): Pair<Result<T>, State>
//
//  override fun invoke(state: State): Pair<Result<T>, State> = parse(state)
//}
//
//inline fun <T> parser(crossinline block: KParserDsl.() -> T): KParser<T> = KParser<T> { state ->
//  val dsl = KParserDsl(state)
//  val result = try {
//    val t = dsl.run(block)
//    Result.Ok(t)
//  } catch (e: ErrException) {
//    e.err
//  }
//  result to dsl.state
//}
//
//
//
//typealias KParserOutput<T> = Pair<Result<T>, State>
//
//class KParserDsl(initial: State) {
//  var state: State = initial
//    private set
//
//  operator fun <T> KParser<T>.invoke(): T {
//    val (result, next) = this.parse(state)
//    return when (result) {
//      is Result.Err -> throw ErrException(result)
//      is Result.Ok<T> -> {
//        this@KParserDsl.state = next
//        result.value
//      }
//    }
//  }
//}
//
///**
// * DO NOT USE THIS CLASS!  Only exposed due to inline functions.
// *
// * Used internally to control execution within a [res] block.
// */
//@PublishedApi
//internal class ErrException internal constructor(val err: Result.Err) : Exception(null as String?) {
//  override fun fillInStackTrace(): Throwable = this
//}
//
