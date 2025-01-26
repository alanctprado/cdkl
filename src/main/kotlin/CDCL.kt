package com.alanprado

class CDCL(private var formula: Formula) {
  private var decisionLevel = 0
  private val model = Model
  private val implicationGraph = ImplicationGraph(ConflictStrategy.DECISIONS)
  private val watcher = TwoLiteralWatcher(formula)

  fun solve(): SolverResult {
    unitPropagation()
    do {
      while (hasConflict()) {
        if (decisionLevel == 0) return SolverResult.Unsatisfiable
        val (learnedClause, level) = analyzeConflict()
        backjump(learnedClause, level)
        unitPropagation()
      }
      if (modelIsPartial()) {
        decide()
        unitPropagation()
      }
    } while (modelIsPartial() || hasConflict())
    return SolverResult.Satisfiable
  }

  private fun analyzeConflict(): Pair<Clause, Int> {
    assert(watcher.conflictClause() != null)
    return implicationGraph.analyzeConflict(watcher.conflictClause()!!)
  }

  private fun backjump(learnedClause: Clause, level: Int) {
    implicationGraph.backjump(level)
    model.backjump(level)
    watcher.backJump(level)
    formula = formula.addClause(learnedClause)
    watcher.addClause(learnedClause)
    decisionLevel = level
  }

  private fun decide() {
    decisionLevel++
    val unassignedLiteral = formula.clauses.flatMap { it.literals }.find { !model.hasVariable(it) }!!   // Move this logic into the model and optimize it
    implicationGraph.addDecision(unassignedLiteral, decisionLevel)
    model.addLiteral(unassignedLiteral, decisionLevel)
    watcher.addLiteral(unassignedLiteral)
  }

  private fun unitPropagation() {
    val unitClause = watcher.unitClause()
    val unitLiteral = unitClause?.literals?.find { !model.hasLiteral(it.opposite()) }
    if (unitLiteral != null) {
      implicationGraph.addImplication(unitLiteral, decisionLevel, unitClause)
      model.addLiteral(unitLiteral, decisionLevel)
      watcher.addLiteral(unitLiteral)
      unitPropagation()
    }
  }

  fun getModel() = model.assignment()

  private fun hasConflict(): Boolean = watcher.conflictClause() != null
  private fun modelIsPartial(): Boolean = model.size() < formula.totalVariables
}