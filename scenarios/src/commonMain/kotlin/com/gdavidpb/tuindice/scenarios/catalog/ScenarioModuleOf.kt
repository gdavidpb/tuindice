package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The coachmark overlay lives in the `wizard` module; its scenarios are the `coachmarks` ones. The assistant's actions
 * are all pending until its own scenarios exist (A11); until then they belong to About, where the assistant is reached.
 */
private val scenarioModuleOfActionModule = mapOf("wizard" to "coachmarks", "assistant" to "about")

/** The scenario module that owns the actions of [actionModule], the module of their `presentation/contract`. */
fun scenarioModuleOf(actionModule: String): String = scenarioModuleOfActionModule[actionModule] ?: actionModule
