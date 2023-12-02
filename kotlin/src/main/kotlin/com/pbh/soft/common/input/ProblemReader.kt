package com.pbh.soft.common.input

import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

object ProblemReader {
    private val daysPath = Path("C:\\Users\\superuser\\code\\advent-of-code\\days")
    private val expectedFileNames = setOf("example", "problem")

    fun readProblem(day: Int): Problem {
        val dayPath = daysPath.resolve("day$day")
        val partInputs = (1..2).asSequence()
            .map { dayPath.resolve("part$it") }
            .filter { it.exists() }
            .map { partPath: Path ->
                val (example, actual) = listOf("example.in", "problem.in").map { Input(partPath.resolve(it)) }
                val others: Map<String, Input> = partPath.listDirectoryEntries().asSequence()
                    .filter { it.name !in expectedFileNames }
                    .sortedBy { it.name }
                    .associateTo(LinkedHashMap()) { it.name to Input(it) }
                Inputs(example, actual, others)
            }
            .toList()
        return Problem(day, partInputs[0], if (partInputs.size == 2) partInputs[1] else null)
    }
}