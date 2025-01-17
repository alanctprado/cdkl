package com.alanprado

import com.alanprado.SolverState.*
import com.alanprado.Value.*

class CDCL(private val clauses: Problem) {
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

  private fun recursiveDpll(clauses: Problem): SolverState {
    if (clauses.isEmpty) return Satisfiable
    if (clauses.hasEmptyClause) return Unsatisfiable
    val unitLiteral = clauses.unitClause?.getLiteral
    if (unitLiteral != null) return recursiveDpll(propagate(clauses, unitLiteral))
    val literal: Literal = clauses.getLiteral!!
    if (recursiveDpll(propagate(clauses, literal)) == Satisfiable) return Satisfiable
    return recursiveDpll(propagate(clauses, literal.opposite()))
  }
}