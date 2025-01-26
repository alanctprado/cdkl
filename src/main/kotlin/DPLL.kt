package com.alanprado

import com.alanprado.SolverResult.*
import com.alanprado.Value.*

class DPLL(private val clauses: Formula) {
  private val model = mutableMapOf<Variable, Value>()

  fun solve(): SolverResult {
    return recursiveDpll(clauses)
  }

  fun getModel(): List<Literal> {
    return model.map { Literal(it.key, if (it.value == True) Polarity.Positive else Polarity.Negative) }
  }

  private fun propagate(clauses: Formula, literal: Literal): Formula {
    model[literal.variable] = if (literal.isPositive) True else False
    return clauses.removeSatisfiedClauses(literal).removeOppositeLiterals(literal)
  }

  private fun recursiveDpll(clauses: Formula): SolverResult {
    if (clauses.isEmpty) return Satisfiable
    if (clauses.hasEmptyClause) return Unsatisfiable
    clauses.unitClause?.getLiteral?.let { return recursiveDpll(propagate(clauses, it)) }
    val literal: Literal = clauses.getLiteral!!
    if (recursiveDpll(propagate(clauses, literal)) == Satisfiable) return Satisfiable
    return recursiveDpll(propagate(clauses, literal.opposite()))
  }
}