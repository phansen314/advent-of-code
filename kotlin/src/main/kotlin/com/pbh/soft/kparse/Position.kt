package com.pbh.soft.kparse

data class Position(
  val index: Index = 0,
  val line: Int = 0,
  val lineBegin: Index = 0
) {
  val column: Int get() = index - lineBegin
}