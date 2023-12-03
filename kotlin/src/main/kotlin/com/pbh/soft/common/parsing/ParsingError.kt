package com.pbh.soft.common.parsing

data class ParsingError(val causes: List<ParseLineError>) : RuntimeException("Unable to parse some lines of input!")
