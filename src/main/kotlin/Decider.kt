package com.alanprado

abstract class Decider(val formula: Formula) {
  abstract fun decide(): Literal
}

class BasicDecider(formula: Formula) : Decider(formula) {
  private val literals: Set<Literal> by lazy { formula.clauses.flatMap { it.literals }.toSet() }

  override fun decide(): Literal {
    val unassignedLiteral = literals.find { !Model.hasVariable(it) }
    return unassignedLiteral ?: throw IllegalStateException("Cannot decide because all variables are assigned")
  }
}