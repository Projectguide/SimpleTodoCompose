package io.github.jisungbin.simpletodocompose

object PathConfig {
    const val AllPath = "TODO-LIST"

    @Suppress("FunctionName")
    fun Task(name: String) = "TODO-LIST/$name"
}
