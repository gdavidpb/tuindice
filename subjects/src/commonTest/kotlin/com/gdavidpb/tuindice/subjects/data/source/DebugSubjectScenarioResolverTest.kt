package com.gdavidpb.tuindice.subjects.data.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DebugSubjectScenarioResolverTest {
	@Test
	fun resolve_supportsRealisticCodes() {
		assertEquals(DebugSubjectScenario.EC5745, DebugSubjectScenarioResolver.resolve("EC5745"))
		assertEquals(DebugSubjectScenario.MAT2230, DebugSubjectScenarioResolver.resolve("MAT2230"))
		assertEquals(DebugSubjectScenario.FIS2105, DebugSubjectScenarioResolver.resolve("FIS2105"))
		assertEquals(DebugSubjectScenario.EL2001, DebugSubjectScenarioResolver.resolve("EL2001"))
		assertEquals(DebugSubjectScenario.EP3421, DebugSubjectScenarioResolver.resolve("EP3421"))
		assertEquals(DebugSubjectScenario.QUI100, DebugSubjectScenarioResolver.resolve("QUI100"))
	}

	@Test
	fun resolve_supportsLegacyDebugAliases() {
		assertEquals(DebugSubjectScenario.EC5745, DebugSubjectScenarioResolver.resolve("DBG-NUM"))
		assertEquals(DebugSubjectScenario.EP3421, DebugSubjectScenarioResolver.resolve("DBG-QUAL"))
		assertEquals(DebugSubjectScenario.QUI100, DebugSubjectScenarioResolver.resolve("DBG-GLOBAL"))
		assertEquals(DebugSubjectScenario.UNAVAILABLE, DebugSubjectScenarioResolver.resolve("DBG-NODATA"))
		assertEquals(DebugSubjectScenario.ERROR, DebugSubjectScenarioResolver.resolve("DBG-ERROR"))
	}

	@Test
	fun resolve_returnsNullForUnknownCodes() {
		assertNull(DebugSubjectScenarioResolver.resolve("USB0001"))
	}
}
