package com.alanprado

object Model {
  private val assignments = mutableMapOf<Literal, Int>()

  override fun toString(): String = assignments.toString()

  fun addLiteral(literal: Literal, level: Int) {
    if (safeMode && hasVariable(literal)) throw IllegalStateException("Model already has assignment for $literal")
    assignments[literal] = level
  }

  fun backjump(level: Int) {
    assignments -= assignments.filter { it.value > level }.keys
  }

  fun size() = assignments.size

  fun hasLiteral(literal: Literal): Boolean = assignments.contains(literal)

  fun hasVariable(literal: Literal): Boolean = hasLiteral(literal) || hasLiteral(literal.opposite())

  fun levelOf(literal: Literal): Int? = assignments[literal]

  fun assignment() = assignments.keys.toList()
}
