package com.pbh.soft.common.parsing

import cc.ekblad.konbini.ParserResult

data class ParseLineError(val lineNum: Int, val error: ParserResult.Error)