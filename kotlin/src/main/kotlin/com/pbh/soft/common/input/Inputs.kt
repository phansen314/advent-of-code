package com.pbh.soft.common.input

data class Inputs(val example: Input, val actual: Input, val others: Map<String, Input>) {
    operator fun get(otherName: String): Input? = others[otherName]
}
//    : Sequence<Input> {
//    override fun iterator(): Iterator<Input> = sequence {
//        yield(example)
//        yield(problem)
//        yieldAll(other)
//    }.iterator()
//}