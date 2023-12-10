package com.pbh.soft.kparse

data class Output<out T>(val result: Result<T>, val next: State) {
  companion object {
    fun <T> ok(t: T, next: State) = Output(Result.Ok(t), next)

    fun err(loc: Loc, message: String, state: State) =
      Output(Result.Err(loc, message), state)
  }
}
