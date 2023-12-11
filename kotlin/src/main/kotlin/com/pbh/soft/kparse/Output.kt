package com.pbh.soft.kparse

data class Output<out T>(val result: Result<T>, val next: State) {
  companion object {
    fun <T> ok(t: T, next: State) = Output(Result.Ok(t), next)

    fun err(position: Position, message: String, state: State) =
      Output(Result.Err(position, message), state)
  }
}
