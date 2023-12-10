package com.pbh.soft.kparse

typealias Index = Int

fun interface KParser<T> : (State) -> Output<T> {
//  fun parse(state: State): Output<T>
//  override fun invoke(state: State): Output<T> = parse(state)

  companion object {
    /* ******************************************************************************************************************
     *                                  PRIMITIVE/BASIS PARSERS
     * ******************************************************************************************************************/
    fun <T> ok(t: T) = KParser { Output.ok(t, it) }
    fun err(message: String) = KParser<Nothing> { throw ErrException(Result.Err(it.loc, message)) }


    /* ******************************************************************************************************************
     *                                  POSITION PARSERS
     * ******************************************************************************************************************/
    val loc = KParser { Output.ok(it.loc, it) }
    val start = KParser {
      if (it.loc.index == 0) Output.ok(Unit, it)
      else Output.err(it.loc, "Expected to be at start of input", it)
    }
    val end = KParser {
      if (it.loc.index == it.input.length) Output.ok(Unit, it)
      else Output.err(it.loc, "Expected to be at end of input", it)
    }

    /* ******************************************************************************************************************
     *                                  SINGLE CHARACTER PARSERS
     * ******************************************************************************************************************/
    val chr = chr { true }
    fun chr(c: Char) = chr { it == c }

    inline fun chr(crossinline block: (Char) -> Boolean) = KParser { st ->
      val (input, loc) = st
      if (loc.index >= input.length) return@KParser Output.err(loc, "Expected to read char!  At end of input", st)
      val c = input[loc.index]
      if (c == '\n')
        Output.ok('\n', st.copy(loc = Loc(loc.index + 1, loc.line + 1, loc.index + 1)))
      else if (c == '\r') {
        if (loc.index + 1 < input.length && input[loc.index + 1] == '\n')
          Output.ok('\n', st.copy(loc = Loc(loc.index + 2, loc.line + 1, loc.index + 2)))
        else
          Output.ok('\n', st.copy(loc = Loc(loc.index + 1, loc.line + 1, loc.index + 1)))
      } else
        if (block(c))
          Output.ok(c, st.copy(loc = loc.copy(index = loc.index + 1)))
        else
          Output.err(loc, "Expected to read char contained in valid!  Char did not match any expected chars", st)
    }

    fun <T> chr(mapping: Map<Char, T>) = KParser<T> { st ->
      val output = chr(st)
      @Suppress("UNCHECKED_CAST")
      when (output.result) {
        is Result.Err -> output as Output<T>
        is Result.Ok -> mapping[output.result.value]
          ?.let { Output.ok(it, output.next) }
          ?: Output.err(st.loc, "Expected a Char to exist in mapping!", st)
      }
    }

    val asciiLowerLetter = 'a'..'z'
    val asciiUpperLetter = 'A'..'Z'
    val hexDigit = '0'..'9'
    val hexLetterLower = 'a'..'f'
    val octalDigit = '0'..'7'

    val letter = chr(Char::isLetter)
    val digit = chr(Char::isDigit)
    val letterOrDigit = chr(Char::isLetterOrDigit)
    val uppercase = chr(Char::isUpperCase)
    val lowercase = chr(Char::isLowerCase)
    val ws = chr(Char::isWhitespace)
    val newline = chr { it == '\n' }
    val hex = chr { it.lowercaseChar() in hexLetterLower || it in hexDigit }
    val octal = chr { it in octalDigit }
    val asciiLower = chr { it in asciiLowerLetter }
    val asciiUpper = chr { it in asciiUpperLetter }
    val ascii = chr { it.lowercaseChar() in asciiLowerLetter }

    fun rgx(pattern: Regex) = KParser { st ->
      val value = pattern.matchAt(st.input, st.loc.index)?.value
      if (value == null) Output.err(st.loc, "Expected to match pattern: $pattern at ${st.loc.index}!", st)
      else Output.ok(value, st.copy(loc = st.loc.copy(index = st.loc.index + value.length)))
    }

    /* ******************************************************************************************************************
     *                                  META/COMBINATOR PARSERS
     * ******************************************************************************************************************/

    inline fun <T> KParser<T>.sat(name: String = "sat", crossinline block: (T) -> Boolean) = KParser<T> { st ->
      val output = this(st)
      when (output.result) {
        is Result.Err -> output
        is Result.Ok -> if (block(output.result.value)) output else Output.err(
          st.loc,
          "Expected sat named $name to be satisfied by value!",
          st
        )
      }
    }

    inline fun <T, U> KParser<T>.map(crossinline block: (T) -> U) = KParser<U> { st ->
      val output = this(st)
      @Suppress("UNCHECKED_CAST")
      when (output.result) {
        is Result.Err -> output as Output<U>
        is Result.Ok -> Output.ok(block(output.result.value), output.next)
      }
    }

    inline fun <T, U> KParser<T>.flatMap(crossinline block: (T) -> KParser<U>) = KParser<U> { st ->
      val output = this(st)
      @Suppress("UNCHECKED_CAST")
      when (output.result) {
        is Result.Err -> output as Output<U>
        is Result.Ok -> block(output.result.value)(output.next)
      }
    }

    fun <T, U> KParser<T>.then(other: KParser<U>) = flatMap { t -> other.map { u -> t to u } }

    inline fun <T> parser(crossinline block: KParserDsl.() -> T): KParser<T> = KParser<T> { state ->
      val dsl = KParserDsl(state)
      val result = try {
        Result.Ok(dsl.run(block))
      } catch (e: ErrException) {
        e.err
      }
      Output(result, dsl.state)
    }

    fun <T> KParser<T>.many(min: Int = 0): KParser<List<T>> = parser {
      val list = mutableListOf<T>()
      while (true) {
        try {
          list.add(this@many())
        } catch (e: ErrException) {
          break
        }
      }
      if (list.size >= min) list
      else err("Expected at least $min occurrences!")
    }

    fun <T, U> KParser<T>.manySep(separator: KParser<U>, min: Int = 0, ): KParser<ManySep<T, U>> = parser {
      val first = try {
        this@manySep()
      } catch (e: ErrException) {
        if (min == 0) return@parser ManySep<T, U>()
        else throw e
      }
      val rest = (separator to this@manySep).many()()
      val values = mutableListOf(first)
      val separators = mutableListOf<U>()
      for ((sep, value) in rest) {
        values.add(value)
        separators.add(sep)
      }

      if (values.size >= min) ManySep(values, separators)
      else err("Expected at least $min occurrences of value KParser!")
    }
  }
}