package com.pbh.soft.kparse

data class State(
  val input: String,
  val position: Position = Position(0, 0, 0)
) {
  fun isDone() = position.index >= input.length
  fun isNotDone() = !isDone()
}