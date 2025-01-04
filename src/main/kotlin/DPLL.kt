package com.alanprado

import com.alanprado.SolverState.*
import com.alanprado.Value.*

class DPLL(private val clauses: Problem) {
  private val model: MutableMap<Variable, Value> =
      clauses.getVariables.associateWith { True }.toMutableMap()

  fun solve() {
    println(recursiveDpll(clauses))
    println(model)
  }

  private fun propagate(problem: Problem, literal: Literal): Problem {
    model[literal.variable] = if (literal.isPositive) True else False
    return problem.removeSatisfiedClauses(literal).removeOppositeLiterals(literal)
  }

  private fun recursiveDpll(unsatClauses: Problem): SolverState {
    if (unsatClauses.isEmpty) return Satisfiable
    if (unsatClauses.hasEmptyClause) return Unsatisfiable
    val unitLiteral = unsatClauses.unitClause?.getLiteral
    if (unitLiteral != null) return recursiveDpll(propagate(unsatClauses, unitLiteral))
    val literal: Literal = unsatClauses.getLiteral!!
    if (recursiveDpll(propagate(unsatClauses, literal)) == Satisfiable) return Satisfiable
    return recursiveDpll(propagate(unsatClauses, literal.opposite()))
  }
}