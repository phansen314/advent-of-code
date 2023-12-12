package com.pbh.soft.common.runner

import com.pbh.soft.common.Solver
import com.pbh.soft.common.input.Input
import com.pbh.soft.common.input.Inputs
import com.pbh.soft.common.input.Problem
import com.pbh.soft.common.input.ProblemReader
import com.pbh.soft.common.parsing.ParsingLinesError
import com.pbh.soft.day1.Day1Solver
import com.pbh.soft.day10.Day10Solver
import com.pbh.soft.day11.Day11Solver
import com.pbh.soft.day2.Day2Solver
import com.pbh.soft.day3.Day3Solver
import com.pbh.soft.day4.Day4Solver
import com.pbh.soft.day5.Day5Solver
import com.pbh.soft.day6.Day6Solver
import com.pbh.soft.day7.Day7Solver
import com.pbh.soft.day8.Day8Solver
import com.pbh.soft.day9.Day9Solver
import mu.KLogging
import kotlin.time.measureTimedValue

enum class DayRunner(val day: Int, val solver: Solver) {
  Day1(1, Day1Solver),
  Day2(2, Day2Solver),
  Day3(3, Day3Solver),
  Day4(4, Day4Solver),
  Day5(5, Day5Solver),
  Day6(6, Day6Solver),
  Day7(7, Day7Solver),
  Day8(8, Day8Solver),
  Day9(9, Day9Solver),
  Day10(10, Day10Solver),
  Day11(11, Day11Solver),
  ;

  companion object : KLogging()

  fun run(vararg runs: Run) {
    logger.info { "Reading input for day$day ..." }
    val problem = ProblemReader.readProblem(day)

    for (run in runs) {
      for (input in problem[run]) {
        logger.info { "Attempting to solve day$day ${run.part}:${run.inputType} @ ${input.path} ..." }
        try {
          val (result, duration) = measureTimedValue {
            when (run.part) {
              Part.P1 -> solver.solveP1(input.text)
              Part.P2 -> solver.solveP2(input.text)
            }
          }
          logger.info { "RESULT: '$result' solved in $duration time" }
        } catch (pe: ParsingLinesError) {
          logger.error { "Unable to parse all of the input!" }
          pe.causes.forEach { (lineNum, error) ->
            logger.error {
              "Unable to parse day$day ${run.part} with input ${run.inputType} line: $lineNum due to $error"
            }
          }
          continue
        } catch (e: Exception) {
          logger.error(e) { "Error running day$day ${run.part} with input ${run.inputType}" }
          continue
        }
      }
    }
  }

  operator fun Inputs.get(type: InputType): Collection<Input> = when (type) {
    InputType.Example -> listOf(example)
    InputType.Actual -> listOf(actual)
    InputType.Others -> others.values
    is InputType.Other -> others["${type.name}.in"]?.let { listOf(it) } ?: emptyList<Input>().also {
      logger.warn { "Unable to find other input named: ${type.name} and this Input will be skipped!" }
    }
  }

  operator fun Problem.get(part: Part): Inputs? = when (part) {
    Part.P1 -> part1
    Part.P2 -> part2
  }

  operator fun Problem.get(run: Run): Collection<Input> {
    val partInputs = this[run.part] ?: return emptyList<Input>().also {
      logger.warn { "${Part.P2} has not been defined and will be skipped during this run!" }
    }
    return partInputs[run.inputType]
  }
}
