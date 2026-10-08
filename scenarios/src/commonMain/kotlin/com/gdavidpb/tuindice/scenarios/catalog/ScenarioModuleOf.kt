package com.gdavidpb.tuindice.scenarios.catalog

/** The coachmark overlay lives in the `wizard` module; its scenarios are the `coachmarks` ones. */
private val scenarioModuleOfActionModule = mapOf("wizard" to "coachmarks")

/** The scenario module that owns the actions of [actionModule], the module of their `presentation/contract`. */
fun scenarioModuleOf(actionModule: String): String = scenarioModuleOfActionModule[actionModule] ?: actionModule
