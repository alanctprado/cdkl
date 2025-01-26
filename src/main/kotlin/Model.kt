package com.alanprado

// REFACTOR MODEL

private data class Assignment(val literal: Literal, val level: Int)

object Model {
  private val assignments = mutableListOf<Assignment>() // Change to Set?

  override fun toString(): String = assignments.toString()

  fun addLiteral(literal: Literal, level: Int) {
    assert(!hasVariable(literal))
    assignments.add(Assignment(literal, level))
  }

  fun backjump(level: Int) {
    assignments -= assignments.filter { it.level > level }.toSet()
  }

  fun size() = assignments.size

  fun hasLiteral(literal: Literal): Boolean = assignments.any { it.literal == literal }

  fun hasVariable(literal: Literal): Boolean = assignments.any { it.literal == literal || it.literal == literal.opposite() }

  fun levelOf(literal: Literal): Int? = assignments.find { it.literal == literal }?.level

  fun assignment() = assignments.map { it.literal }
}
