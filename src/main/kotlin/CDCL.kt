package com.alanprado

import com.alanprado.SolverResult.*

data class Assignment(val literal: Literal, val level: Int) {}

class Model() {
  private val assignments = mutableListOf<Assignment>()

  override fun toString(): String = assignments.toString()

  fun addLiteral(literal: Literal, level: Int) {
    assert(!hasVariable(literal))
    assignments.add(Assignment(literal, level))
  }

  fun backJump(level: Int) {
    assignments -= assignments.filter { it.level > level }.toSet()
  }

  fun size() = assignments.size

  fun hasLiteral(literal: Literal): Boolean = assignments.any { it.literal == literal }

  fun hasVariable(literal: Literal): Boolean = assignments.any { it.literal == literal || it.literal == literal.opposite() }

  fun assignment() = assignments.toList()
}

object SolverState {
  private val formula = mutableListOf<Clause>()
  private val model = Model()
}

class Watcher() {
  // TODO: convert model and formula to singleton
  fun conflictClause(model: Model, formula: Formula): Clause? = formula.clauses
    .find { clause -> clause.literals
      .all { literal -> model.hasLiteral(literal.opposite()) } }

  // TODO: convert model and formula to singleton
  fun unitClause(model: Model, formula: Formula): Clause? = formula.clauses
    .filterNot { clause -> clause.literals
      .any { literal -> model.hasLiteral(literal) }}
    .find { clause -> clause.literals
      .count { literal -> model.hasLiteral(literal.opposite()) } == clause.size - 1 }
}

class CDCL(private var formula: Formula) {
  var decisionLevel = 0
  val model = Model()
  val implicationGraph = ImplicationGraph(ConflictStrategy.DECISIONS)
  val watcher = Watcher()

  fun solve(): SolverResult {
    unitPropagation()
    do {
      while (hasConflict()) {
        if (decisionLevel == 0) return Unsatisfiable
        val (learnedClause, level) = analyzeConflict()
        backJump(learnedClause, level)
        unitPropagation()
      }
      if (modelIsPartial()) {
        decide()
        unitPropagation()
      }
    } while (modelIsPartial() || hasConflict())
    return Satisfiable
  }

  private fun analyzeConflict(): Pair<Clause, Int> {
    assert(watcher.conflictClause(model, formula) != null)
    return implicationGraph.analyzeConflict(watcher.conflictClause(model, formula)!!)
  }

  private fun backJump(learnedClause: Clause, level: Int) {
    model.backJump(level)
    implicationGraph.backjump(level)
    formula = formula.addClause(learnedClause)
    decisionLevel = level
  }

  private fun decide() {
    decisionLevel++
    val unassignedLiteral = formula.clauses.flatMap { it.literals }.find { !model.hasVariable(it) }!!
    implicationGraph.addDecision(unassignedLiteral, decisionLevel)
    model.addLiteral(unassignedLiteral, decisionLevel)
  }

  private fun unitPropagation() {
    val unitClause = watcher.unitClause(model, formula)
    val unitLiteral = unitClause?.literals?.find { !model.hasLiteral(it.opposite()) }
    if (unitLiteral != null) {
      implicationGraph.addImplication(unitLiteral, decisionLevel, unitClause)
      model.addLiteral(unitLiteral, decisionLevel)
      unitPropagation()
    }
  }

  fun getModel() = model.assignment()

  private fun hasConflict(): Boolean = watcher.conflictClause(model, formula) != null
  private fun modelIsPartial(): Boolean = model.size() < formula.totalVariables

  // Input: CNF F
  // model = empty, decision_level = 0, dstack:=lambda x.0 -- records how big was the assignment when the decision was made
  // m = unitPropagation(m, F);
  // do
  //   # backtracking
  //   while there is conflict
  //     if dl = 0 then return unsat
  //     (C, dl) = analyzeConflict(m, F); -- returns a conflict clause learned using implication graph and a decision level upto which the solver needs to backtrack
  //     m.resize(dstack(dl)); F := F U {C}
  //     m := unitPropagation(m, F)
  //
  //   # boolean decision
  //   if m is partial then
  //     dstack(dl) := m.size();
  //     dl += 1
  //     decide(m, F) -- chooses an unassigned variable in m and assigns a boolean value
  //     unitPropagation(m, F)
  // while m is partial or m does not model F
  // return sat
}