package com.gdavidpb.tuindice.scenarios.fixture

/** Where a [Copy] text comes from, so a test can fail when it stops matching. */
sealed interface CopyBinding {
	val text: String

	/**
	 * A string resource of [module] (`<module>/src/commonMain/composeResources/values/strings.xml`).
	 * [key] is the resource name, with `[one]`/`[other]` appended for a plural quantity; [arguments] fill
	 * its `%1$s` / `%1$d` placeholders in order.
	 */
	data class Resource(
		override val text: String,
		val module: String,
		val key: String,
		val arguments: List<String> = emptyList()
	) : CopyBinding

	/** Text the mock data supplies (a subject name, say); the repo-relative [file] must contain it. */
	data class MockData(override val text: String, val file: String) : CopyBinding

	/** Text the scenario itself hands the app through a launch argument; [by] says which. */
	data class Supplied(override val text: String, val by: String) : CopyBinding

	/** Text the app computes at runtime from data (a formatted period, say); [reason] says how. */
	data class Derived(override val text: String, val reason: String) : CopyBinding
}
