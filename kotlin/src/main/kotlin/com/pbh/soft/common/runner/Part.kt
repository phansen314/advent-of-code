package com.pbh.soft.common.runner

enum class Part {
    P1, P2;

    fun example() = Run(this, InputType.Example)
    fun actual() = Run(this, InputType.Actual)
    fun others() = Run(this, InputType.Others)
    fun other(name: String) = Run(this, InputType.Other(name))
}