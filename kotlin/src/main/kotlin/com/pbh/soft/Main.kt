package com.pbh.soft

import com.pbh.soft.common.runner.DayRunner
import com.pbh.soft.common.runner.DayRunner.*
import com.pbh.soft.common.runner.Part
import com.pbh.soft.common.runner.Part.P1
import com.pbh.soft.common.runner.Part.P2

fun main(args: Array<String>) {
  Main().run()
}

class Main : Runnable {
  override fun run() {
    Day18.run(P2.problem())
  }
}