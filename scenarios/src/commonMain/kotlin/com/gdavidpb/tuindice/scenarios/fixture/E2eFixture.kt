package com.gdavidpb.tuindice.scenarios.fixture

/**
 * An entity id (or code) a scenario looks up. [declaredIn] lists the mock files (repo-relative) that
 * serve it; an id the app computes instead has no file and says how in [derivedFrom].
 */
data class E2eFixture(
	val value: String,
	val declaredIn: List<String>,
	val derivedFrom: String? = null
) {
	init {
		require(declaredIn.isNotEmpty() || derivedFrom != null) {
			"Fixture '$value' needs a mock file or a derivation"
		}
	}

	companion object {
		fun derived(value: String, reason: String) = E2eFixture(value, emptyList(), reason)
	}
}
