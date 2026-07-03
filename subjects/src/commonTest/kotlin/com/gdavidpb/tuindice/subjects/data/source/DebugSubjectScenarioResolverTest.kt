package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DebugSubjectScenarioResolverTest {
	@Test
	fun resolve_supportsRealisticCodes() {
		assertEquals(DebugSubjectScenario.EC5745, DebugSubjectScenarioResolver.resolve("EC5745")?.scenario)
		assertEquals(DebugSubjectScenario.MAT2230, DebugSubjectScenarioResolver.resolve("MAT2230")?.scenario)
		assertEquals(DebugSubjectScenario.FIS2105, DebugSubjectScenarioResolver.resolve("FIS2105")?.scenario)
		assertEquals(DebugSubjectScenario.EL2001, DebugSubjectScenarioResolver.resolve("EL2001")?.scenario)
		assertEquals(DebugSubjectScenario.EP3421, DebugSubjectScenarioResolver.resolve("EP3421")?.scenario)
		assertEquals(DebugSubjectScenario.QUI100, DebugSubjectScenarioResolver.resolve("QUI100")?.scenario)
	}

	@Test
	fun resolve_supportsLegacyDebugAliases() {
		assertEquals(DebugSubjectScenario.EC5745, DebugSubjectScenarioResolver.resolve("DBG-NUM")?.scenario)
		assertEquals(DebugSubjectScenario.EP3421, DebugSubjectScenarioResolver.resolve("DBG-QUAL")?.scenario)
		assertEquals(DebugSubjectScenario.QUI100, DebugSubjectScenarioResolver.resolve("DBG-GLOBAL")?.scenario)
		assertEquals(DebugSubjectScenario.UNAVAILABLE, DebugSubjectScenarioResolver.resolve("DBG-NODATA")?.scenario)
		assertEquals(DebugSubjectScenario.ERROR, DebugSubjectScenarioResolver.resolve("DBG-ERROR")?.scenario)
	}

	@Test
	fun resolve_infersMetadataForRecordSubjects() {
		val resolved = assertNotNull(DebugSubjectScenarioResolver.resolve("EC5344"))

		assertEquals(DebugSubjectScenario.EC5745, resolved.scenario)
		assertEquals("EC5344", resolved.metadata?.code)
		assertEquals("RADIACION Y ANTENAS", resolved.metadata?.name)
		assertEquals(3, resolved.metadata?.credits)
		assertEquals(GradingMode.NUMERIC, resolved.metadata?.gradingMode)
	}

	@Test
	fun resolve_infersMetadataForCurrentRecordSubjects() {
		val resolved = assertNotNull(DebugSubjectScenarioResolver.resolve("CI4325"))

		assertEquals(DebugSubjectScenario.EC5745, resolved.scenario)
		assertEquals("CI4325", resolved.metadata?.code)
		assertEquals("INTERFACES CON EL USUARIO", resolved.metadata?.name)
		assertEquals(5, resolved.metadata?.credits)
		assertEquals(GradingMode.NUMERIC, resolved.metadata?.gradingMode)
	}

	@Test
	fun resolve_returnsNullForRemoteMockRecordSubjects() {
		assertNull(DebugSubjectScenarioResolver.resolve("EC5751"))
		assertNull(DebugSubjectScenarioResolver.resolve("QA"))
		assertNull(DebugSubjectScenarioResolver.resolve("QB"))
	}

	@Test
	fun resolve_infersQualitativeScenarioForRecordSubjects() {
		val resolved = assertNotNull(DebugSubjectScenarioResolver.resolve("EP5406"))

		assertEquals(DebugSubjectScenario.EP3421, resolved.scenario)
		assertEquals("EP5406", resolved.metadata?.code)
		assertEquals(9, resolved.metadata?.credits)
		assertEquals(GradingMode.QUALITATIVE_PASS_FAIL, resolved.metadata?.gradingMode)
	}

	@Test
	fun resolve_infersQualitativeScenarioForCurrentRecordSubjects() {
		val resolved = assertNotNull(DebugSubjectScenarioResolver.resolve("EP1308"))

		assertEquals(DebugSubjectScenario.EP3421, resolved.scenario)
		assertEquals("EP1308", resolved.metadata?.code)
		assertEquals(3, resolved.metadata?.credits)
		assertEquals(GradingMode.QUALITATIVE_PASS_FAIL, resolved.metadata?.gradingMode)
	}

	@Test
	fun resolve_returnsNullForUnknownCodes() {
		assertNull(DebugSubjectScenarioResolver.resolve("USB0001"))
	}
}
