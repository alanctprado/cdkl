package com.alanprado

import com.github.ajalt.clikt.core.InvalidFileFormat
import java.io.File
import kotlin.math.absoluteValue

class Parser(private val filePath: String) {
  private val file = File(filePath)

  init {
    require(file.exists())  { "File does not exist: $filePath" }
  }

  fun validateDimacsFormat() {
    val lines = file.readLines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) throwFormattingError("File is empty.")

    if (lines.count { it.startsWith("p ") } != 1) throwFormattingError("Expected exactly one problem ('p') line.")
    val problemLine = lines.find { it.startsWith("p ") }!!
    val (expectedVariables, expectedClauses) = parseAndValidateProblemLine(problemLine)

    val clauses = lines.filterNot { it.startsWith("c ") || it.startsWith("p ") }
    validateClauses(clauses, expectedVariables, expectedClauses)
  }

  private fun throwFormattingError(details: String) {
    throw InvalidFileFormat(
      filePath,
      """The input file is not in the correct DIMACS format:
      $details""".trimIndent()
    )
  }

  private fun parseAndValidateProblemLine(line: String): Pair<Int, Int> {
    if (!Regex("""^p\s+cnf\s+\d+\s+\d+$""").matches(line))
      throwFormattingError("Problem line is not valid: $line")
    val problemStatement = line.split(Regex("""\s+"""))
    return problemStatement[2].toInt() to problemStatement[3].toInt()
  }

  private fun validateClauses(clauses: List<String>, expectedVariables: Int, expectedClauses: Int) {
    val variables = mutableSetOf<Int>()
    for (clause in clauses) {
      if (!Regex("""^(-?\d+\s+)+0$""").matches(clause)) throwFormattingError("Invalid line: $clause")
      val clauseVariables = clause.split(Regex("""\s+""")).map { it.toInt().absoluteValue }
      if (clauseVariables.indexOfFirst { it == 0 } != clauseVariables.lastIndex) throwFormattingError("'0' can only occur in the end of a clause: $clause")
      variables.addAll(clauseVariables.dropLast(1))
    }
    if (clauses.size != expectedClauses) throwFormattingError("Number of clauses (${clauses.size}) does not match the expected value ($expectedClauses).")
    if (variables.size != expectedVariables) throwFormattingError("Number of variables (${variables.size}) does not match the expected value ($expectedVariables).")
  }

}