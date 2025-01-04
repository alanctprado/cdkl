package com.alanprado

import com.alanprado.Polarity.*

enum class SolverState { Satisfiable, Unsatisfiable }
enum class Polarity { Positive, Negative }
enum class Value { True, False, Unknown }

data class Variable(val index: Int)

data class Literal(val variable: Variable, val polarity: Polarity) {
  val isPositive = polarity == Positive
  val isNegative = polarity == Negative
}

fun Literal.opposite(): Literal =
  if (polarity == Positive) Literal(variable, Negative)
  else Literal(variable, Positive)

class Clause(private val literals: List<Literal>) {
  override fun toString(): String = literals.joinToString("; ")
  val isEmpty: Boolean = literals.isEmpty()
  val isUnit: Boolean = literals.size == 1
  val getLiteral: Literal? = literals.firstOrNull()
  fun contains(literal: Literal) = literals.contains(literal)
  fun remove(literal: Literal) = Clause(literals.filterNot { it == literal })
  val getVariables: List<Variable> = literals.map { it.variable }
}

class Problem(private val clauses: List<Clause>) {
  override fun toString(): String = clauses.joinToString("\n")
  val isEmpty: Boolean = clauses.isEmpty()
  val hasEmptyClause: Boolean = clauses.any { it.isEmpty }
  val unitClause: Clause? = clauses.find { it.isUnit }
  val getLiteral: Literal? = clauses.firstOrNull()?.getLiteral
  val getVariables: List<Variable> =
      clauses.flatMap { it.getVariables }.toSet().toList()

  fun removeSatisfiedClauses(literal: Literal): Problem =
      Problem(clauses.filterNot { it.contains(literal) })
  fun removeOppositeLiterals(literal: Literal): Problem =
      Problem(clauses.map { it.remove(literal.opposite()) })
}