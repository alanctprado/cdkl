package com.alanprado

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument

class CdklCommand : CliktCommand() {
  private val file: String by argument()

  override fun run() {
    val parser = Parser(file)
    parser.validateDimacsFormat()
    val problem = parser.parseProblem()
    println(problem)
  }
}

fun main(args: Array<String>) = CdklCommand().main(args)