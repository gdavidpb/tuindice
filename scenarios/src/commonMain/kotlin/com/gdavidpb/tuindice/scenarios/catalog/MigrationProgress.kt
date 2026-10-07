package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The scenario modules whose Maestro flows are not translated yet. `ActionCoverageTest` does not require
 * their actions to be covered; removing a module from [pendingModules] makes the test demand every one of
 * them, which is how a translation batch proves it left nothing out. Deleted at the cut-over, when the
 * test becomes unconditional.
 */
object MigrationProgress {
	val pendingModules: Set<String> = setOf(
		"auth",
		"maincore",
		"coachmarks",
		"enrollmentproof",
		"pensum",
		"subjects",
		"evaluations",
		"about"
	)

	/** The coachmark overlay lives in the `wizard` module; its scenarios are the `coachmarks` ones. */
	private val scenarioModuleOfActionModule = mapOf("wizard" to "coachmarks")

	/** The scenario module that owns the actions of [actionModule], the module of their `presentation/contract`. */
	fun scenarioModuleOf(actionModule: String): String = scenarioModuleOfActionModule[actionModule] ?: actionModule
}
