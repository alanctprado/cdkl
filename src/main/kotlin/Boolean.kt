package com.alanprado

import com.alanprado.Polarity.*

enum class SolverResult { Satisfiable, Unsatisfiable }
enum class Polarity { Positive, Negative }
enum class Value { True, False, Unknown }

data class Variable(val index: Int) {
  override fun toString(): String = "x$index"
}

data class Literal(val variable: Variable, val polarity: Polarity) {
  val isPositive = polarity == Positive
  val isNegative = polarity == Negative
  override fun toString() = "${if (polarity == Positive) "" else "~"}$variable"
}

fun Literal.opposite(): Literal =
  if (polarity == Positive) Literal(variable, Negative)
  else Literal(variable, Positive)

class Clause(val literals: List<Literal>) {
  override fun toString(): String = "(${literals.joinToString(" ")})"
  val isEmpty: Boolean = literals.isEmpty()
  val isUnit: Boolean = literals.size == 1
  val getLiteral: Literal? = literals.firstOrNull()
  fun contains(literal: Literal) = literals.contains(literal)
  fun remove(literal: Literal) = Clause(literals.filterNot { it == literal })
  val variables: List<Variable> = literals.map { it.variable }
  val size = literals.size
}

class Formula(val clauses: List<Clause>) {
  override fun toString(): String = clauses.joinToString("\n")
  val isEmpty: Boolean = clauses.isEmpty()
  val hasEmptyClause: Boolean = clauses.any { it.isEmpty }
  val unitClause: Clause? = clauses.find { it.isUnit }
  val getLiteral: Literal? = clauses.firstOrNull()?.getLiteral
  val variables: List<Variable> =
      clauses.flatMap { it.variables }.toSet().toList()
  val totalVariables: Int

  init {
    totalVariables = variables.size
  }

  fun removeSatisfiedClauses(literal: Literal): Formula =
      Formula(clauses.filterNot { it.contains(literal) })
  fun removeOppositeLiterals(literal: Literal): Formula =
      Formula(clauses.map { it.remove(literal.opposite()) })
  fun addClause(clause: Clause): Formula =
      Formula(clauses + clause)
}