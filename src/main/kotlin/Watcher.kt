package com.alanprado

abstract class Watcher(formula: Formula) {
  val watchedClauses: MutableList<Clause> = formula.clauses.toMutableList()
  var numClauses: Int = formula.size
  abstract fun addClause(clause: Clause)
  abstract fun addLiteral(literal: Literal)
  abstract fun backJump(level: Int)
  abstract fun conflictClause(): Clause?
  abstract fun unitClause(): Pair<Literal, Clause>?
}

class BasicWatcher(formula: Formula) : Watcher(formula) {
  override fun addClause(clause: Clause) {
    watchedClauses.add(clause)
    numClauses++
  }

  override fun conflictClause() = watchedClauses.find { clause ->
    clause.literals.all { literal -> Model.hasLiteral(literal.opposite()) }
  }

  override fun unitClause(): Pair<Literal, Clause>? = watchedClauses.filterNot { clause ->
    clause.literals.any { literal -> Model.hasLiteral(literal) }
  }.find { clause ->
    clause.literals.count { literal -> Model.hasLiteral(literal.opposite()) } == clause.size - 1
  }?.let { clause ->
    clause.literals.find { !Model.hasLiteral(it.opposite()) }!! to clause
  }

  override fun addLiteral(literal: Literal) {}
  override fun backJump(level: Int) {}
}

class TwoLiteralWatcher(formula: Formula) : Watcher(formula) {
  data class SatisfiedClause(val index: Int, val level: Int)
  data class UnsatisfiedClause(val index: Int, val watchedLiterals: List<Literal>) {
    override fun toString(): String = watchedLiterals.toString()
  }

  private val satisfiedClauses = mutableListOf<SatisfiedClause>()
  private val unsatisfiedClauses = mutableListOf<UnsatisfiedClause>()

  init {
    for ((index, clause) in watchedClauses.withIndex()) {
      processClause(index, clause)
    }
    if (safeMode) validateWatcher()
  }

  override fun addClause(clause: Clause) {
    watchedClauses.add(clause)
    numClauses++
    processClause(numClauses - 1, clause)
    if (safeMode) validateWatcher()
  }

  override fun addLiteral(literal: Literal) {
    val badIndices =
      unsatisfiedClauses.filter { clause -> literal.variable in clause.watchedLiterals.map { it.variable } }
        .map { it.index }
    unsatisfiedClauses.removeIf { clause -> literal.variable in clause.watchedLiterals.map { it.variable } }
    for (index in badIndices) processClause(index, watchedClauses[index])
    if (safeMode) validateWatcher()
  }

  override fun backJump(level: Int) {
    val badIndices = satisfiedClauses.filter { it.level > level }.map { it.index }
    satisfiedClauses.removeIf { it.level > level }
    for (index in badIndices) processClause(index, watchedClauses[index])
    if (safeMode) validateWatcher()
  }

  override fun conflictClause(): Clause? =
    unsatisfiedClauses.find { it.watchedLiterals.isEmpty() }?.index?.let { watchedClauses[it] }

  override fun unitClause(): Pair<Literal, Clause>? =
    unsatisfiedClauses.find { it.watchedLiterals.size == 1 }?.let { it.watchedLiterals[0] to watchedClauses[it.index] }

  private fun processClause(index: Int, clause: Clause) {
    val unassignedLiterals = mutableListOf<Literal>()
    var minimumLevel = -1
    for (literal in clause.literals) {
      val level = Model.levelOf(literal)
      if (level != null) {
        minimumLevel = if (minimumLevel == -1) level else minOf(minimumLevel, level)
      } else if (unassignedLiterals.size < 2 && !Model.hasLiteral(literal.opposite())) {
        unassignedLiterals.add(literal)
      }
    }
    if (minimumLevel != -1) satisfiedClauses.add(SatisfiedClause(index, minimumLevel))
    else unsatisfiedClauses.add(UnsatisfiedClause(index, unassignedLiterals))
  }

  private fun validateWatcher() {
    val foundClauses = mutableSetOf<Int>()
    for (clause in satisfiedClauses) {
      if (clause.index < 0 || clause.index >= numClauses) throw IllegalStateException("Invalid index found in clause $clause")
      if (!foundClauses.add(clause.index)) throw IllegalStateException("Clause $clause duplicated.")
    }
    for (clause in unsatisfiedClauses) {
      if (clause.index < 0 || clause.index >= numClauses) throw IllegalStateException("Invalid index found in clause $clause")
      if (!foundClauses.add(clause.index)) throw IllegalStateException("Clause $clause duplicated.")
    }
    if (foundClauses.size != numClauses) throw IllegalStateException("Invalid number of clauses: found ${foundClauses.size} instead of $numClauses")
  }
}
