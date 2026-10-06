package com.gdavidpb.tuindice.scenariokit

import com.gdavidpb.tuindice.scenariokit.dsl.ifVisible
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CallSiteTest {
	private val start = LaunchSpec(emptyMap())

	@Test
	fun everyStepCarriesTheFileAndLineOfTheCallerNotOfTheDsl() {
		val callerLine = Throwable("line probe").stackTrace.first().lineNumber + 1
		val built = scenario("a-b", "a", start) { tap("button") }

		val site = assertNotNull(built.steps.single().site)

		assertEquals("CallSiteTest.kt", site.file)
		assertEquals(callerLine, site.line)
	}

	@Test
	fun nestedBlocksReportTheirOwnLines() {
		val built = scenario("a-b", "a", start) {
			ifVisible("banner") {
				tap("dismiss")
			}
		}

		val outer = assertNotNull(built.steps.single().site)
		val inner = assertNotNull((built.steps.single() as Step.IfVisible).steps.single().site)

		assertEquals("CallSiteTest.kt", inner.file)
		assertTrue(inner.line > outer.line, "inner $inner should be below outer $outer")
	}
}
