# Conflict-Driven Kotlin Learning

> **Disclaimer**: This project is in its early stages of development.

**Conflict-Driven Kotlin Learning** is a project inspired by the book *Atomic Kotlin* by Bruce Eckel and Svetlana Isakova. A project implementing a Conflict-Driven Clause Learning (CDCL) solver was chosen to explore the practical application of the book's concepts.

Kotlin's expressiveness makes it possible to create a highly readable and approachable implementation of a CDCL solver, allowing anyone with basic programming knowledge to explore and understand it easily.

## Project Goals

This project aims to have the following characteristics:

1. **Ease of understanding and use:** The codebase should enable individuals with no prior SAT-solving experience to understand both foundational and advanced concepts. The code should be self-explanatory, minimizing the need for external documentation to understand the purpose of classes or functions.

2. **Theoretical documentation**: Provide comprehensive documentation covering the theoretical foundations, including method complexity, proofs of termination, and other relevant concepts.This should be part of the code, but kept concise to preserve readability.

3. **Encourage extensibility**: Offer a modular structure that allows developers to experiment with different heuristics in CDCL solving. Automated tests and benchmarks should assist in verifying implementation and enable performance comparisons.

## Current Features

- Support for the [DIMACS CNF input format](https://people.sc.fsu.edu/~jburkardt/data/cnf/cnf.html) and output in the [SAT competition format](https://satcompetition.github.io/2024/output.html).
- Basic **DPLL SAT solver**.
- Basic **CDCL solver** with:
  - Clause learning.
  - Non-chronological backjumping.
- Unit propagation using **two watched literals**.

## Future Plans

- Enhance the DPLL SAT solver.
- Add comprehensive theoretical documentation for all implemented features, aligning with the project's second goal.
- Implement additional CDCL features, including:
  - First UIPs (Unique Implication Points).
  - VSIDS (Variable State Independent Decaying Sum) heuristic for literal decision-making.
- Improve the overall project structure and build system.
