// package com.alanprado
//
// import com.alanprado.SolverState.*
// import com.alanprado.Value.*
//
// class DPLL(private val clauses: Formula) {
//   private val model: MutableMap<Variable, Value> =
//       clauses.getVariables.associateWith { True }.toMutableMap()
//
//   fun solve() {
//     println(recursiveDpll(clauses))
//     println(model)
//   }
//
//   private fun propagate(formula: Formula, literal: Literal): Formula {
//     model[literal.variable] = if (literal.isPositive) True else False
//     return formula.removeSatisfiedClauses(literal).removeOppositeLiterals(literal)
//   }
//
//   private fun recursiveDpll(formula: Formula): SolverState {
//     if (formula.isEmpty) return Satisfiable
//     if (formula.hasEmptyClause) return Unsatisfiable
//     formula.unitClause?.getLiteral?.let { return recursiveDpll(propagate(formula, it)) }
//     val literal: Literal = formula.getLiteral!!
//     if (recursiveDpll(propagate(formula, literal)) == Satisfiable) return Satisfiable
//     return recursiveDpll(propagate(formula, literal.opposite()))
//   }
// }