package com.pbh.soft.common.parsing

import cc.ekblad.konbini.ParserResult

data class ParsingLinesError(val causes: List<ParseLineError>) : RuntimeException("Unable to parse some lines of input!")
data class ParsingError(val causes: ParserResult.Error) : RuntimeException("Unable to parse!")
