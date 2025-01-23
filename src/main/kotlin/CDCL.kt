package com.alanprado

import com.alanprado.SolverState.*

data class Assignment(val literal: Literal, val level: Int) {}

class Model() {
  private val assignments = mutableListOf<Assignment>()

  override fun toString(): String {
    return assignments.toString()
  }

  fun addLiteral(literal: Literal, level: Int) {
    assert(!hasVariable(literal))
    assignments.add(Assignment(literal, level))
  }

  fun backJump(level: Int) {
    assignments.dropLastWhile { it.level > level }
  }

  fun size() = assignments.size

  fun hasLiteral(literal: Literal): Boolean = assignments.any { it.literal == literal }

  fun hasVariable(literal: Literal): Boolean = assignments.any { it.literal == literal || it.literal == literal.opposite() }

  fun assignment() = assignments.toList()
}

class ImplicationGraph() {
  data class Node(val literal: Literal, val type: String, val decisionLevel: Int)
  private val invertedGraph = mutableMapOf<Node, MutableList<Node>>()

  fun decide(literal: Literal, level: Int) {
    invertedGraph[Node(literal, "Decision", level)] = mutableListOf<Node>()
  }

  fun propagate(unitLiteral: Literal, level: Int, clause: Clause) {
    assert (invertedGraph.keys.none { it.literal == unitLiteral })
    assert (invertedGraph.keys.none { it.literal == unitLiteral.opposite() })
    val unitNode = Node(unitLiteral, "Propagation", level)
    invertedGraph[unitNode] = mutableListOf<Node>()
    assert(clause.literals
      .filterNot{ it == unitLiteral }
      .all { literal -> invertedGraph.keys
        .count {node -> node.literal == literal.opposite() } == 1 })
    for (literal in clause.literals) {
      if (literal == unitLiteral) continue
      invertedGraph[unitNode]!!.add(invertedGraph.keys.find { it.literal == literal.opposite() }!!)
    }
  }

  private fun findDecisions(sources: List<Node>): Set<Node> {
    val queue = sources.toMutableList()
    val visited = mutableSetOf<Node>()
    val decisions = mutableSetOf<Node>()
    while (queue.isNotEmpty()) {
      val next = queue.removeFirst()
      when {
        next.type == "Decision" -> decisions.add(next)
        next in visited -> continue
        else -> {
          assert(invertedGraph.contains(next))
          assert(next.type == "Propagation")
          for (node in invertedGraph[next]!!) queue.add(node)
          visited.add(next)
        }
      }
    }
    return decisions
  }

  fun analyzeConflict(conflictClause: Clause): Pair<Clause, Int> {
    assert(conflictClause.literals.size >= 2)
    assert(conflictClause.literals
      .all { literal -> invertedGraph.keys
        .count { node -> node.literal == literal.opposite() } == 1 })
    val sources = conflictClause.literals
      .map { literal -> invertedGraph.keys
        .find { node -> node.literal == literal.opposite() }!! }
    assert(sources.size == conflictClause.literals.size)
    val conflictingDecisions = findDecisions(sources)
    val secondToLastLevel = conflictingDecisions
      .map { it.decisionLevel }
      .sortedDescending()[1]
    val learnedClause = Clause(conflictingDecisions.map { it.literal.opposite() })
    return Pair(learnedClause, secondToLastLevel)
  }

  fun backJump(level: Int) {
    invertedGraph.filterNot { (key, _) -> key.decisionLevel > level }
    assert(invertedGraph.keys.all { it.decisionLevel <= level })
  }

  // DAG
  // Nodes: set of true literals under the current model, and a conflict node
  // Edges: {(l1, l2) | l2 was created due to unit propagation and l1 is falsified in the clause i.e. contributed to unit propagation
  //                    and l2 != ~l1 }
  // E.G. if we have (x1 or x2) and ~x1 is set to true, we get an edge ~x1 -> x2
  // - Each node is annotated with the decision level
  // E.G. if we have (x1 or x2) and ~x1 is set to true at decision level k, we get an edge ~x1@k -> x2@k i.e. same level
  // - Each edge is labeled with the corresponding clause
  // - Nodes can be differentiated between decisions and propagations. Decisions have no incoming edges.
  // methods: add decision(literal)
  //          add unit propagation(literal, clause)
  //          conflict (clause)
  // - we traverse the implication graph backwards to find the set of decisions that caused the conflict
  // - the clause of the negations of the causing decisions is called conflict clause (we can backtrack to find all decisions -- nodes without incoming edges. then, we flip the signals)
  // - add conflict clause in the input clauses and backtrack to the second last conflicting decision, and proceed like DPLL
}

class Watcher(private val model: Model, private val formula: Formula) {
  fun conflictClause(): Clause? = formula.clauses
    .find { clause -> clause.literals
      .all { literal -> model.hasLiteral(literal.opposite()) } }

  fun unitClause(): Clause? = formula.clauses
    .filterNot { clause -> clause.literals
      .any { literal -> model.hasLiteral(literal) }}
    .find { clause -> clause.literals
      .count { literal -> model.hasLiteral(literal.opposite()) } == clause.size - 1 }
}

class CDCL(private var formula: Formula) {
  var decisionLevel = 0
  val model = Model()
  val implicationGraph = ImplicationGraph()
  val watcher = Watcher(model, formula)

  fun solve(): SolverState {
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
    assert(watcher.conflictClause() != null)
    return implicationGraph.analyzeConflict(watcher.conflictClause()!!)
  }

  private fun backJump(learnedClause: Clause, level: Int) {
    model.backJump(level)
    implicationGraph.backJump(level)
    formula = formula.addClause(learnedClause)
  }

  private fun decide() {
    decisionLevel++
    val unassignedLiteral = formula.clauses.flatMap { it.literals }.find { !model.hasVariable(it) }!!
    implicationGraph.decide(unassignedLiteral, decisionLevel)
    model.addLiteral(unassignedLiteral, decisionLevel)
  }

  private fun unitPropagation() {
    val unitClause = watcher.unitClause()
    val unitLiteral = unitClause?.literals?.find { !model.hasLiteral(it.opposite()) }
    if (unitLiteral != null) {
      implicationGraph.propagate(unitLiteral, decisionLevel, unitClause)
      model.addLiteral(unitLiteral, decisionLevel)
      unitPropagation()
    }
  }

  fun getModel() = model.assignment()

  private fun hasConflict(): Boolean = watcher.conflictClause() != null
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