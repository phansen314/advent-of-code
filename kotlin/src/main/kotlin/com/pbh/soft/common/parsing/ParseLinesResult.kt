package com.pbh.soft.common.parsing

data class ParseLinesResult<T>(val values: List<T>, val errors: List<ParseLineError>)