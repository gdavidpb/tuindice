package com.gdavidpb.tuindice.subjects.data.source

internal object DebugSubjectScenarioResolver {
	private val aliases = mapOf(
		"EC5745" to DebugSubjectScenario.EC5745,
		"DBG-NUM" to DebugSubjectScenario.EC5745,
		"MAT2230" to DebugSubjectScenario.MAT2230,
		"DBG-RET" to DebugSubjectScenario.MAT2230,
		"FIS2105" to DebugSubjectScenario.FIS2105,
		"DBG-FIS" to DebugSubjectScenario.FIS2105,
		"EL2001" to DebugSubjectScenario.EL2001,
		"DBG-EASY" to DebugSubjectScenario.EL2001,
		"EP3421" to DebugSubjectScenario.EP3421,
		"DBG-QUAL" to DebugSubjectScenario.EP3421,
		"QUI100" to DebugSubjectScenario.QUI100,
		"DBG-GLOBAL" to DebugSubjectScenario.QUI100,
		"MAT404" to DebugSubjectScenario.UNAVAILABLE,
		"DBG-NODATA" to DebugSubjectScenario.UNAVAILABLE,
		"ERR500" to DebugSubjectScenario.ERROR,
		"DBG-ERROR" to DebugSubjectScenario.ERROR
	)

	fun normalize(subjectCode: String): String {
		return subjectCode.trim().uppercase()
	}

	fun resolve(subjectCode: String): DebugSubjectScenario? {
		return aliases[normalize(subjectCode)]
	}
}
