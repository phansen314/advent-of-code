package com.pbh.soft.kparse

data class State(
  val input: String,
  val loc: Loc = Loc(0, 0, 0)
)