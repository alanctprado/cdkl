package com.alanprado

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import java.io.File

class CdklCommand : CliktCommand() {
  private val file: String by argument()

  override fun run() {
    val file = File(file)
    if (file.exists()) {
      file.forEachLine { println(it) }
    } else {
      throw IllegalArgumentException("File does not exist")
    }
  }
}

fun main(args: Array<String>) = CdklCommand().main(args)