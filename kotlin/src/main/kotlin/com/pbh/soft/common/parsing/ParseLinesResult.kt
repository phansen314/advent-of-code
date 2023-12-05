package com.pbh.soft.common.parsing

data class ParseLinesResult<T>(val values: T, val errors: List<ParseLineError>)