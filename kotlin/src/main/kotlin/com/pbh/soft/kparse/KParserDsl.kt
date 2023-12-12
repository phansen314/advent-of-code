package com.pbh.soft.kparse

import com.pbh.soft.kparse.KParser.Companion.keepL
import com.pbh.soft.kparse.KParser.Companion.many
import com.pbh.soft.kparse.KParser.Companion.manySep
import com.pbh.soft.kparse.KParser.Companion.map
import com.pbh.soft.kparse.KParser.Companion.opt
import com.pbh.soft.kparse.KParser.Companion.or
import com.pbh.soft.kparse.KParser.Companion.sat
import com.pbh.soft.kparse.KParser.Companion.then

class KParserDsl(initial: State) {
  var state: State = initial
    private set

  fun isDone() = state.isDone()
  fun isNotDone() = state.isNotDone()

  operator fun <T> KParser<T>.invoke(): T {
    val (result, next) = this(state)
    return when (result) {
      is Result.Err -> throw ErrException(result)
      is Result.Ok<T> -> {
        this@KParserDsl.state = next
        result.value
      }
    }
  }

  infix fun <T, U> KParser<T>.to(other: KParser<U>) = this.then(other)

  fun err(message: String): Nothing = throw ErrException(Result.Err(state.position, message))

  fun chr(c: Char) = KParser.chr(c)()
  inline fun chr(crossinline block: (Char) -> Boolean) = KParser.chr(block)()
  fun <T> chr(mapping: Map<Char, T>) = KParser.chr(mapping)()
  fun letter() = KParser.letter()
  fun digit() = KParser.digit()
  fun letterOrDigit() = KParser.letterOrDigit()
  fun uppercase() = KParser.uppercase()
  fun lowercase() = KParser.lowercase()
  fun ws() = KParser.ws()
  fun newline() = KParser.newline()
  fun hex() = KParser.hex()
  fun octal() = KParser.octal()
  fun asciiLower() = KParser.asciiLower()
  fun asciiUpper() = KParser.asciiUpper()
  fun ascii() = KParser.ascii()
  operator fun Regex.invoke() = KParser.rgx(this)()

  fun Char.p() = KParser.chr { it == this }
  fun String.anyChar() = KParser.chr { it in this }
  fun String.noChar() = KParser.chr { it !in this }

  inline fun <T> sat(parser: KParser<T>, name: String = "sat", crossinline block: (T) -> Boolean) = parser.sat(name, block)()
  inline fun <T, U> map(parser: KParser<T>, crossinline block: (T) -> U) = parser.map(block)()
  fun <T, U> pair(parser: KParser<T>, other: KParser<U>) = parser.then(other)()
  fun <T> many(parser: KParser<T>, min: Int = 0) = parser.many(min)()
  fun <T, U> manySep(parser: KParser<T>, separator: KParser<U>, min: Int = 0) = parser.manySep(separator, min)()
  fun <T> any(parser: KParser<T>, vararg others: KParser<T>) = parser.or(*others)()
  fun <T> opt(parser: KParser<T>) = parser.opt()()
  fun <T> keepL(parser: KParser<T>, other: KParser<*>) = parser.keepL(other)()
}