package com.pbh.soft.common.input

import java.nio.file.Path
import kotlin.io.path.readText

data class Input(val path: Path, val text: String) {
    companion object {
        operator fun invoke(path: Path) = Input(path, path.readText())
    }
}
