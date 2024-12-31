package com.alanprado

enum class Polarity { Positive, Negative }
enum class Value { True, False, Unknown }

data class Variable(val index: Int, val polarity: Polarity, var value: Value = Value.Unknown) {
  val isPositive = polarity == Polarity.Positive
  val isNegative = polarity == Polarity.Negative
}

class Clause(val variables: List<Variable>) {
  override fun toString(): String = variables.joinToString("; ")
}

class Problem(val clauses: List<Clause>) {
  override fun toString(): String = clauses.joinToString("\n")
}