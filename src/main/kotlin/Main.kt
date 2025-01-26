package com.alanprado

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.alanprado.SolverResult.*

const val safeMode = true

class CdklCommand : CliktCommand() {
  private val file: String by argument()

  override fun run() {
    val problem = Parser(file)
      .apply { validateDimacsFormat() }
      .parseProblem()
    CDCL(problem).apply {
      when(solve()) {
        Unsatisfiable -> printUnsatisfiableResult()
        Satisfiable -> printSatisfiableResult(getModel())
      }
    }
  }

  private fun printSatisfiableResult(model: List<Literal>) {
    println("s SATISFIABLE")
    val literals = model.map { if (it.isPositive) it.variable.index else -it.variable.index }
    println(literals.joinToString(" "))
  }

  private fun printUnsatisfiableResult() {
    println("s UNSATISFIABLE")
  }
}

fun main(args: Array<String>) = CdklCommand().main(args)