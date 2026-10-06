package com.gdavidpb.tuindice.scenarios.fixture

/** An entity id a scenario looks up, with the mock files (repo-relative) that declare it. */
data class E2eFixture(val value: String, val declaredIn: List<String>)
