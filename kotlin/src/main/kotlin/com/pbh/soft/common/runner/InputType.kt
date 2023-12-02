package com.pbh.soft.common.runner

sealed class InputType {
    data object Example : InputType()
    data object Actual : InputType()
    data object Others : InputType()
    data class Other(val name: String): InputType()
}