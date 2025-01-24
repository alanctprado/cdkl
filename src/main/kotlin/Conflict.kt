package com.alanprado

enum class ConflictStrategy { DECISIONS, FIRST_UIP }

private enum class NodeType { DECISION, IMPLICATION }

private data class Node(val literal: Literal, val type: NodeType, val decisionLevel: Int) {
  override fun toString(): String = "${if (type == NodeType.DECISION) "D:" else "P:"}$literal@$decisionLevel"
}

class ImplicationGraph(private val strategy: ConflictStrategy, private val safeMode: Boolean = true) {
  private val implicationGraph = mutableMapOf<Node, MutableList<Node>>()

  fun addDecision(literal: Literal, level: Int) {
    if (safeMode) ensureVariableNotInGraph(literal)
    implicationGraph[Node(literal, NodeType.DECISION, level)] = mutableListOf<Node>()
  }

  fun addImplication(unitLiteral: Literal, level: Int, clause: Clause) {
    if (safeMode) {
      ensureVariableNotInGraph(unitLiteral)
      ensureAllOtherLiteralsOccurWithOppositePolarity(unitLiteral, clause)
    }
    val unitNode = Node(unitLiteral, NodeType.IMPLICATION, level)
    implicationGraph[unitNode] = mutableListOf<Node>()
    for (literal in clause.literals) {
      if (literal == unitLiteral) continue
      implicationGraph[unitNode]!!.add(implicationGraph.keys.find { it.literal == literal.opposite() }!!)
    }
  }

  fun analyzeConflict(conflictClause: Clause): Pair<Clause, Int> {
    if (safeMode) ensureConflictIsValid(conflictClause)
    val sources = conflictClause.literals.map(this::findOppositeNode)
    val conflictReason = resolveConflict(sources)
    val backjumpLevel = findBackjumpLevel(conflictReason)
    val learnedClause = Clause(conflictReason.map { it.literal.opposite() })
    return Pair(learnedClause, backjumpLevel)
  }

  fun backjump(level: Int) {
    implicationGraph -= implicationGraph.keys.filter { it.decisionLevel > level }.toSet()
    if (safeMode) ensureAllDecisionLevelsBelow(level)
  }

  private fun findDecisions(sources: List<Node>): Set<Node> {
    val queue = sources.toMutableList()
    val visited = mutableSetOf<Node>()
    val decisions = mutableSetOf<Node>()
    while (queue.isNotEmpty()) {
      val next = queue.removeFirst()
      if (next in visited) continue
      when (next.type) {
        NodeType.DECISION -> decisions.add(next)
        NodeType.IMPLICATION -> for (node in implicationGraph[next]!!) queue.add(node)
      }
      visited.add(next)
    }
    return decisions
  }

  private fun findOppositeNode(literal: Literal): Node =
    implicationGraph.keys.find { it.literal == literal.opposite() }!!

  private fun resolveConflict(sources: List<Node>): Set<Node> = when (strategy) {
    ConflictStrategy.DECISIONS -> findDecisions(sources)
    else -> throw IllegalArgumentException("Conflict strategy is not implemented")
  }

  private fun findBackjumpLevel(conflict: Set<Node>): Int =
    if (conflict.size > 1) conflict.map { it.decisionLevel }.sortedDescending()[1]
    else 0

  private fun ensureConflictIsValid(conflictClause: Clause) {
    if (conflictClause.literals.isEmpty()) throw IllegalArgumentException("Empty conflict clause")
    val isMissingOppositeLiteral =
      conflictClause.literals.any { literal -> implicationGraph.keys.count { node -> node.literal == literal.opposite() } != 1 }
    if (isMissingOppositeLiteral) {
      throw IllegalArgumentException("Conflicting literals for clause $conflictClause were not found in the graph.")
    }
  }

  private fun ensureVariableNotInGraph(literal: Literal) {
    val isLiteralPresent = implicationGraph.keys.any { it.literal.variable == literal.variable }
    if (isLiteralPresent) {
      throw IllegalStateException("Literal $literal or its opposite exists in the graph.")
    }
  }

  private fun ensureAllDecisionLevelsBelow(level: Int) {
    val hasLevelAbove = implicationGraph.keys.any { it.decisionLevel > level }
    if (hasLevelAbove) {
      throw IllegalStateException("Found a decision level above $level in the implication graph.")
    }
  }

  private fun ensureAllOtherLiteralsOccurWithOppositePolarity(unitLiteral: Literal, clause: Clause) {
    val hasInvalidLiteral = clause.literals.filterNot { it == unitLiteral }.any { literal ->
      implicationGraph.keys.count { node -> node.literal == literal.opposite() } != 1
    }
    if (hasInvalidLiteral) {
      throw IllegalStateException(
        "Not all literals in the clause occur with the opposite polarity of $unitLiteral in the implication graph."
      )
    }
  }
}