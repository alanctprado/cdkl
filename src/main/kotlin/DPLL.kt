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

  private fun propagate(formula: Formula, literal: Literal): Formula {
    model[literal.variable] = if (literal.isPositive) True else False
    return formula.removeSatisfiedClauses(literal).removeOppositeLiterals(literal)
  }

  private fun recursiveDpll(formula: Formula): SolverResult {
    if (formula.isEmpty) return Satisfiable
    if (formula.hasEmptyClause) return Unsatisfiable
    formula.unitClause?.getLiteral?.let { return recursiveDpll(propagate(formula, it)) }
    val literal: Literal = formula.getLiteral!!
    if (recursiveDpll(propagate(formula, literal)) == Satisfiable) return Satisfiable
    return recursiveDpll(propagate(formula, literal.opposite()))
  }
}