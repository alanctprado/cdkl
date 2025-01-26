package com.alanprado

import com.alanprado.SolverResult.*

class CDCL(formula: Formula) {
  private var decisionLevel = 0
  private val implicationGraph = ImplicationGraph(ConflictStrategy.DECISIONS)
  private val watcher = TwoLiteralWatcher(formula)
  private val decider = BasicDecider(formula)
  private val numVariables = formula.numVariables

  fun solve(): SolverResult {
    unitPropagation()
    while (true) {
      if (hasConflict()) { if (!resolveConflict()) return Unsatisfiable }
      else if (modelIsPartial()) decision()
      else return Satisfiable
      unitPropagation()
    }
  }

  private fun resolveConflict(): Boolean {
    if (safeMode && !hasConflict()) throw IllegalStateException("Conflict not found")
    if (decisionLevel == 0) return false
    val (learnedClause, level) = analyzeConflict()
    backjump(level)
    learn(learnedClause)
    return true
  }

  private fun analyzeConflict(): Pair<Clause, Int> {
    if (safeMode && watcher.conflictClause() == null) throw IllegalStateException("Conflict clause not found")
    return implicationGraph.analyzeConflict(watcher.conflictClause()!!)
  }

  private fun backjump(level: Int) {
    implicationGraph.backjump(level)
    Model.backjump(level)
    watcher.backJump(level)
    decisionLevel = level
  }

  private fun learn(learnedClause: Clause) {
    watcher.addClause(learnedClause)
  }

  private fun decision() {
    decisionLevel++
    val unassignedLiteral = decider.decide()
    implicationGraph.addDecision(unassignedLiteral, decisionLevel)
    Model.addLiteral(unassignedLiteral, decisionLevel)
    watcher.addLiteral(unassignedLiteral)
  }

  private fun unitPropagation() {
    while (true) {
      val (unitLiteral, unitClause) = watcher.unitClause() ?: break
      implicationGraph.addImplication(unitLiteral, decisionLevel, unitClause)
      Model.addLiteral(unitLiteral, decisionLevel)
      watcher.addLiteral(unitLiteral)
    }
  }

  fun getModel() = Model.assignment()

  private fun hasConflict(): Boolean = watcher.conflictClause() != null  // TODO: optimize this check
  private fun modelIsPartial(): Boolean = Model.size() < numVariables
}