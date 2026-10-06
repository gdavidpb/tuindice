package com.gdavidpb.tuindice.scenariokit.codec

import kotlin.test.Test
import kotlin.test.assertEquals

class ScenarioNamingTest {
	@Test
	fun swiftClassName_isTheCapitalisedModuleWithTheSuffix() {
		assertEquals("AuthScenarioTests", ScenarioNaming.swiftClassName("auth"))
		assertEquals("EnrollmentproofScenarioTests", ScenarioNaming.swiftClassName("enrollmentproof"))
	}

	@Test
	fun swiftMethodName_replacesDashesWithUnderscores() {
		assertEquals("test_auth_login_success", ScenarioNaming.swiftMethodName("auth-login-success"))
		assertEquals(
			"test_record_synthetic_term_search_empty",
			ScenarioNaming.swiftMethodName("record-synthetic-term-search-empty")
		)
	}

	@Test
	fun swiftMethodName_isInjectiveOverValidIds() {
		val ids = listOf("a-b", "ab", "a-b-c", "ab-c", "a-bc")

		assertEquals(ids.size, ids.map(ScenarioNaming::swiftMethodName).toSet().size)
	}

	@Test
	fun iosOnlyTesting_selectsExactlyOneTest() {
		assertEquals(
			"TuIndiceUITests/AuthScenarioTests/test_auth_login_success",
			ScenarioNaming.iosOnlyTesting(sampleScenario())
		)
	}

	@Test
	fun androidScenarioArg_isTheId() {
		assertEquals("auth-login-success", ScenarioNaming.androidScenarioArg(sampleScenario()))
	}
}
