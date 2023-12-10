package com.pbh.soft.kparse

data class ManySep<T, U>(val values: List<T> = listOf(), val separators: List<U> = listOf())
