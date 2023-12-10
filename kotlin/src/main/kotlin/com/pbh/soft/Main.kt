package com.pbh.soft

import com.pbh.soft.common.runner.DayRunner.Day9
import com.pbh.soft.common.runner.Part
import com.pbh.soft.common.runner.Part.P1
import com.pbh.soft.common.runner.Part.P2

fun main(args: Array<String>) {
  Main().run()
}

class Main : Runnable {
  override fun run() {
//        Day1.run(P1.example(), P1.problem(), P2.other("overlap"), P2.example(), P2.problem())
//    Day2.run(P1.example(), P1.problem(), P2.example(), P2.problem())
//    Day3.run(P1.example(), P1.problem(), P2.example(), P2.problem())
//    Day4.run(P1.example(), P1.problem(), P2.example(), P2.problem())
//    Day5.run(P2.problem())//, P2.example(), P2.problem())
//    Day6.run(P2.example(), P2.problem())
//    Day7.run(P1.problem(), P2.problem())
//    Day8.run(P2.example(), P2.other("other"), P2.problem())
    Day9.run(P2.example(), P2.problem())
  }
}