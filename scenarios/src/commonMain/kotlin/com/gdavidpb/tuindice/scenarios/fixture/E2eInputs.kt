package com.gdavidpb.tuindice.scenarios.fixture

/**
 * Values several scenarios hand to the app and the tags they build from them, named once so two copies
 * cannot drift apart. The ones that depend on what the mocks serve say which.
 */
object E2eInputs {
	/** Where a tap on an attempt's grade slider raises the grade: near its end. */
	const val SliderHighX = 0.95

	/** Where a tap on an attempt's grade slider lowers the grade again: further in. */
	const val SliderLowX = 0.75

	/** The vertical middle of the slider, where its track is. */
	const val SliderMiddleY = 0.5

	/** The grade the primary attempt of the canonical record shows before the high tap. */
	const val GradeBefore = 4

	/** The grade the high tap leaves the primary attempt with. */
	const val GradeAfter = 5

	/** The year of the older pensum the version dialog lists after the current one (served by the 2018 mocks). */
	const val OlderPensumYear = 2018

	/** The modality id of the pensum the scenarios pick in the dialog (served by the 2018 mocks). */
	const val LongInternship = "long_internship"

	/** A day of the month the evaluation date picker always offers. */
	const val CalendarDay = 15

	/** `EvaluationType.TEST`, as the type chip tag spells it. */
	const val EvaluationTypeTest = "test"
}
