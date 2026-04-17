package com.gdavidpb.tuindice.subjects.data.source

internal enum class DebugSubjectScenario(
	val resourcePath: String?
) {
	EC5745(resourcePath = "files/subjects/ec5745.json"),
	MAT2230(resourcePath = "files/subjects/mat2230.json"),
	FIS2105(resourcePath = "files/subjects/fis2105.json"),
	EL2001(resourcePath = "files/subjects/el2001.json"),
	EP3421(resourcePath = "files/subjects/ep3421.json"),
	QUI100(resourcePath = "files/subjects/qui100.json"),
	UNAVAILABLE(resourcePath = "files/subjects/unavailable.json"),
	ERROR(resourcePath = null)
}
