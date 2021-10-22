package io.github.jisungbin.simpletodocompose

import java.io.File

data class Task(val name: String, val done: Boolean = false)

fun List<File>.toTasks() =
    map { file -> Task(name = file.name, done = Storage.read(file.path, "false").toBoolean()) }
